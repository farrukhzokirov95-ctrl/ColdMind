package com.coldmind.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.random.Random

private val Black = Color(0xFF080C12)
private val Panel = Color(0xFF141B25)
private val Panel2 = Color(0xFF1D2938)
private val Blue = Color(0xFF72B7ED)
private val Muted = Color(0xFF91A0B1)
private val White = Color(0xFFE8F1FA)

private val darkColors = darkColorScheme(primary = Blue, background = Black, surface = Panel, onSurface = White, onBackground = White, secondary = Muted)

private data class Workout(val title: String, val exercises: List<String>, val minutes: Int)
private val workouts = listOf(
    Workout("Сила и устойчивость", listOf("Разминка — 8 минут", "Приседания — 3 × 10", "Отжимания — 3 × 8–12", "Тяга резинки — 3 × 10", "Планка — 3 × 30 секунд", "Спокойное дыхание — 5 минут"), 50),
    Workout("Реакция и мышление", listOf("Спокойное дыхание — 5 минут", "Равновесие — 5 минут", "Случайные команды с партнёром — 10 минут", "Деэскалация — 10 минут", "Разбор ошибок — 5 минут"), 35),
    Workout("Кардио и восстановление", listOf("Быстрая ходьба или велосипед — 25 минут", "Мобилизация суставов — 10 минут", "Ориентация в пространстве — 5 минут"), 40),
    Workout("Единоборства и дистанция", listOf("Разминка с тренером", "Устойчивое положение и работа ног", "Контроль дистанции и отход", "Безопасные упражнения с партнёром", "Заминка и разбор"), 55),
    Workout("Экстренные сценарии", listOf("Выбери сценарий", "Оцени угрозу и пути отхода", "Проговори вызов помощи", "Разыграй безопасные действия", "Запиши выводы"), 35),
    Workout("Активное восстановление", listOf("Прогулка или лёгкая растяжка — 30 минут"), 30),
    Workout("Отдых", listOf("Восстановление и сон", "Короткий анализ недели"), 10)
)
private data class Scenario(val title: String, val description: String, val options: List<String>, val correct: Int, val explanation: String)
private val scenarios = listOf(
    Scenario("Агрессия на улице", "Незнакомец повышает голос и быстро приближается. Выход свободен.", listOf("Ответить оскорблением", "Увеличить дистанцию и спокойно уйти", "Подойти ближе и спорить"), 1, "Безопасная дистанция и выход из конфликта обычно предпочтительнее эскалации."),
    Scenario("Дым в коридоре", "Из соседнего коридора идёт густой дым. Есть альтернативный безопасный выход.", listOf("Пойти сквозь дым", "Спрятаться в лифте", "Воспользоваться безопасным выходом и вызвать помощь"), 2, "Избегай дыма, эвакуируйся по безопасному маршруту и вызывай пожарных."),
    Scenario("Человек без сознания", "Человек лежит неподвижно. Место безопасно.", listOf("Проверить реакцию и нормальное дыхание, вызвать помощь", "Дать ему воду", "Оставить без проверки"), 0, "Проверь реакцию и дыхание, вызови экстренную помощь. При отсутствии нормального дыхания начинай СЛР по изученному алгоритму."),
    Scenario("Ссора в помещении", "Два человека кричат друг на друга. Ты не вовлечён в конфликт.", listOf("Вмешаться физически", "Встать между ними", "Сохранить дистанцию и при угрозе вызвать помощь"), 2, "Не увеличивай риск для себя; при необходимости привлекай помощь."),
    Scenario("Заблокированный выход", "Во время эвакуации привычный выход перекрыт.", listOf("Оценить альтернативный безопасный путь", "Бежать в толпу без оценки", "Вернуться за вещами"), 0, "Выбирай доступный безопасный маршрут; не возвращайся за вещами."),
    Scenario("Подозрительный шум", "Ночью слышен громкий удар за дверью. Причина неизвестна.", listOf("Немедленно открыть дверь", "Сначала оценить обстановку из безопасного места", "Выключить телефон"), 1, "Сначала получи информацию без лишнего риска, затем действуй по ситуации."),
    Scenario("Падение человека", "Человек упал на лестнице и жалуется на сильную боль в шее.", listOf("Резко поднять его", "Попросить активно повернуть шею", "Не перемещать без необходимости, вызвать помощь"), 2, "При подозрении на травму позвоночника избегай ненужного перемещения и вызывай специалистов."),
    Scenario("Угроза в транспорте", "Пассажир агрессивен, рядом доступна кнопка связи с водителем.", listOf("Сообщить водителю и увеличить дистанцию", "Спровоцировать пассажира", "Схватить его первым"), 0, "Привлечение персонала и безопасное дистанцирование снижают риск эскалации.")
)

private class Store(context: Context) {
    private val prefs = context.getSharedPreferences("cold_mind_local", Context.MODE_PRIVATE)
    var startDate: String
        get() = prefs.getString("start", null) ?: LocalDate.now().toString().also { prefs.edit().putString("start", it).apply() }
        set(value) { prefs.edit().putString("start", value).apply() }
    fun getInt(key: String) = prefs.getInt(key, 0)
    fun inc(key: String) { prefs.edit().putInt(key, getInt(key) + 1).apply() }
    fun isDone(key: String) = prefs.getBoolean(key, false)
    fun setDone(key: String, done: Boolean) { prefs.edit().putBoolean(key, done).apply() }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(this)
        setContent { MaterialTheme(colorScheme = darkColors) { ColdMindApp(store) } }
    }
}

@Composable private fun ColdMindApp(store: Store) {
    var page by remember { mutableIntStateOf(0) }
    var refresh by remember { mutableIntStateOf(0) }
    val week = remember(store.startDate) { ((ChronoUnit.DAYS.between(LocalDate.parse(store.startDate), LocalDate.now()).coerceAtLeast(0) / 7) + 1).toInt().coerceIn(1, 12) }
    Scaffold(containerColor = Black, bottomBar = {
        Row(Modifier.fillMaxWidth().background(Panel).navigationBarsPadding().padding(vertical = 9.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("ПЛАН", "ТРЕНИРОВКИ", "РЕШЕНИЯ", "ДЫХАНИЕ", "ПРОГРЕСС").forEachIndexed { i, name ->
                Text(name, color = if (page == i) Blue else Muted, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { page = i }.padding(vertical = 10.dp, horizontal = 3.dp))
            }
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Column(Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 12.dp)) {
                Text("COLD MIND", fontSize = 27.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp, color = White)
                Text("ХОЛОДНЫЙ РАЗУМ  /  НЕДЕЛЯ $week ИЗ 12", color = Blue, fontSize = 11.sp, letterSpacing = 1.sp)
            }
            when (page) {
                0 -> PlanScreen(store, week, refresh) { refresh++ }
                1 -> WorkoutScreen(store, week, refresh) { refresh++ }
                2 -> DecisionScreen(store) { refresh++ }
                3 -> BreathingScreen(store) { refresh++ }
                else -> StatsScreen(store, week, refresh)
            }
        }
    }
}

@Composable private fun Header(title: String, subtitle: String) {
    Text(title, color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
    Spacer(Modifier.height(5.dp))
    Text(subtitle, color = Muted, fontSize = 13.sp)
    Spacer(Modifier.height(16.dp))
}
@Composable private fun Tile(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier.fillMaxWidth().background(Panel, RoundedCornerShape(16.dp)).padding(16.dp), content = content)
}
@Composable private fun Primary(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    Button(onClick = onClick, enabled = enabled, colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Black), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) { Text(text, fontWeight = FontWeight.Bold) }
}
private fun phase(week: Int) = when (week) { in 1..4 -> "ФУНДАМЕНТ"; in 5..8 -> "АДАПТАЦИЯ"; else -> "ИНТЕГРАЦИЯ" }
private fun phaseInfo(week: Int) = when (week) {
    in 1..4 -> "Осваивай технику, дыхание и базовые алгоритмы без спешки."
    in 5..8 -> "Добавляй умеренную нагрузку, случайные команды и ограничение времени."
    else -> "Совмещай навыки в безопасных сценариях с партнёром или тренером."
}
@Composable private fun PlanScreen(store: Store, currentWeek: Int, refresh: Int, onChange: () -> Unit) {
    var selectedWeek by remember { mutableIntStateOf(currentWeek) }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Header("План подготовки", "12 недель системной практики")
            Tile {
                Text("ЭТАП: ${phase(selectedWeek)}", color = Blue, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text("Неделя $selectedWeek / 12", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(phaseInfo(selectedWeek), color = Muted, fontSize = 13.sp)
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(progress = { selectedWeek / 12f }, modifier = Modifier.fillMaxWidth(), color = Blue, trackColor = Panel2)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { selectedWeek = (selectedWeek - 1).coerceAtLeast(1) }) { Text("← Ранее") }
                    TextButton(onClick = { selectedWeek = (selectedWeek + 1).coerceAtMost(12) }) { Text("Позже →") }
                }
            }
        }
        itemsIndexed(workouts) { index, workout ->
            val key = "week_${selectedWeek}_day_$index"
            val done = store.isDone(key)
            Tile {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(listOf("ПОНЕДЕЛЬНИК", "ВТОРНИК", "СРЕДА", "ЧЕТВЕРГ", "ПЯТНИЦА", "СУББОТА", "ВОСКРЕСЕНЬЕ")[index], color = Blue, fontSize = 10.sp)
                        Text(workout.title, fontWeight = FontWeight.Bold)
                        Text("${workout.minutes} мин • ${workout.exercises.size} заданий", color = Muted, fontSize = 12.sp)
                    }
                    Checkbox(checked = done, onCheckedChange = { store.setDone(key, it); onChange() }, colors = CheckboxDefaults.colors(checkedColor = Blue))
                }
            }
        }
        item { Text("Отмечай день только после выполнения. Можно просматривать все 12 недель.", color = Muted, fontSize = 12.sp) }
    }
}
@Composable private fun WorkoutScreen(store: Store, week: Int, refresh: Int, onChange: () -> Unit) {
    var day by remember { mutableIntStateOf((LocalDate.now().dayOfWeek.value - 1).coerceIn(0, 6)) }
    var seconds by remember { mutableIntStateOf(0) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running) { while (running) { delay(1000); seconds++ } }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            Header("Физическая подготовка", "Техника важнее скорости и количества")
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEachIndexed { i, label ->
                    OutlinedButton(onClick = { day = i; seconds = 0; running = false }, colors = ButtonDefaults.outlinedButtonColors(contentColor = if (day == i) Blue else Muted)) { Text(label) }
                }
            }
        }
        item {
            Tile {
                Text(workouts[day].title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("Ориентир: ${workouts[day].minutes} минут", color = Muted)
                Spacer(Modifier.height(10.dp))
                Text("%02d:%02d".format(seconds / 60, seconds % 60), fontSize = 44.sp, fontWeight = FontWeight.Bold, color = Blue)
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { running = !running }, colors = ButtonDefaults.buttonColors(containerColor = Blue, contentColor = Black)) { Text(if (running) "Пауза" else "Старт") }
                    OutlinedButton(onClick = { running = false; seconds = 0 }) { Text("Сброс") }
                }
            }
        }
        itemsIndexed(workouts[day].exercises) { i, exercise ->
            val key = "exercise_${week}_${day}_$i"
            Tile {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(exercise, Modifier.weight(1f), fontSize = 14.sp)
                    Checkbox(checked = store.isDone(key), onCheckedChange = { store.setDone(key, it); onChange() }, colors = CheckboxDefaults.colors(checkedColor = Blue))
                }
            }
        }
        item {
            Primary(if (store.isDone("week_${week}_day_$day")) "ДЕНЬ ВЫПОЛНЕН ✓" else "ЗАВЕРШИТЬ ДЕНЬ") {
                store.setDone("week_${week}_day_$day", true); running = false; onChange()
            }
        }
    }
}
@Composable private fun DecisionScreen(store: Store, onChange: () -> Unit) {
    var scenarioIndex by remember { mutableIntStateOf(Random.nextInt(scenarios.size)) }
    var selected by remember { mutableIntStateOf(-1) }
    var elapsed by remember { mutableIntStateOf(0) }
    var active by remember { mutableStateOf(true) }
    LaunchedEffect(scenarioIndex, active) { while (active && selected == -1) { delay(1000); elapsed++ } }
    val scenario = scenarios[scenarioIndex]
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Header("Тренажёр решений", "Выбирай безопасное действие, не поддаваясь импульсу")
        Tile {
            Text("СЦЕНАРИЙ ${scenarioIndex + 1} / ${scenarios.size}", color = Blue, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Text(scenario.title, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(scenario.description, color = Muted)
            Spacer(Modifier.height(12.dp))
            Text("Время размышления: ${elapsed} с", color = Blue)
        }
        Spacer(Modifier.height(16.dp))
        scenario.options.forEachIndexed { i, answer ->
            OutlinedButton(onClick = {
                if (selected == -1) {
                    selected = i; active = false; store.inc("decisions"); if (i == scenario.correct) store.inc("correct"); onChange()
                }
            }, enabled = selected == -1, modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp), shape = RoundedCornerShape(12.dp)) {
                Text("${i + 1}. $answer", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Start)
            }
        }
        if (selected != -1) {
            Tile {
                Text(if (selected == scenario.correct) "ВЕРНОЕ РЕШЕНИЕ" else "ЕСТЬ БОЛЕЕ БЕЗОПАСНЫЙ ВАРИАНТ", color = if (selected == scenario.correct) Blue else Color(0xFFFFC77B), fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Text(scenario.explanation)
            }
            Spacer(Modifier.height(12.dp))
            Primary("СЛЕДУЮЩИЙ СЦЕНАРИЙ") {
                scenarioIndex = (scenarioIndex + 1 + Random.nextInt(scenarios.size - 1)) % scenarios.size
                selected = -1; elapsed = 0; active = true
            }
        }
        Spacer(Modifier.height(12.dp))
        Text("Это учебные сценарии, а не замена очной подготовке по первой помощи и безопасности.", color = Muted, fontSize = 12.sp)
    }
}
@Composable private fun BreathingScreen(store: Store, onChange: () -> Unit) {
    var running by remember { mutableStateOf(false) }
    var seconds by remember { mutableIntStateOf(0) }
    var completed by remember { mutableStateOf(false) }
    val duration = 180
    LaunchedEffect(running) {
        while (running && seconds < duration) { delay(1000); seconds++ }
        if (running && seconds >= duration && !completed) { running = false; completed = true; store.inc("breathing"); onChange() }
    }
    val phaseSecond = seconds % 10
    val phaseText = if (phaseSecond < 4) "ВДОХ" else "ВЫДОХ"
    val remain = if (phaseSecond < 4) 4 - phaseSecond else 10 - phaseSecond
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Header("Контроль дыхания", "Мягкий ритм 4 секунды вдох / 6 секунд выдох")
        Spacer(Modifier.height(30.dp))
        Box(Modifier.size(250.dp).border(2.dp, Blue, RoundedCornerShape(125.dp)).background(Panel, RoundedCornerShape(125.dp)), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (running) phaseText else "ГОТОВ?", color = Blue, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                Text(if (running) "$remain" else "4 / 6", fontSize = 52.sp, fontWeight = FontWeight.Light)
                Text("СПОКОЙНО • БЕЗ ЗАДЕРЖКИ", color = Muted, fontSize = 10.sp)
            }
        }
        Spacer(Modifier.height(26.dp))
        Text("%02d:%02d / 03:00".format(seconds / 60, seconds % 60), fontSize = 23.sp, color = White)
        Spacer(Modifier.height(12.dp))
        LinearProgressIndicator(progress = { seconds / duration.toFloat() }, color = Blue, trackColor = Panel2, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(20.dp))
        Primary(if (running) "ПАУЗА" else if (completed) "ПОВТОРИТЬ" else "НАЧАТЬ") {
            if (completed) { seconds = 0; completed = false; running = true } else running = !running
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = { running = false; seconds = 0; completed = false }, modifier = Modifier.fillMaxWidth()) { Text("Сброс") }
        Spacer(Modifier.height(18.dp))
        Tile { Text("Дыши комфортно, без усилия. Если появляется головокружение или дискомфорт — остановись и вернись к обычному дыханию.", color = Muted, fontSize = 13.sp) }
    }
}
@Composable private fun StatsScreen(store: Store, week: Int, refresh: Int) {
    val done = (1..12).sumOf { w -> (0..6).count { d -> store.isDone("week_${w}_day_$d") } }
    val attempts = store.getInt("decisions")
    val correct = store.getInt("correct")
    val breath = store.getInt("breathing")
    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { Header("Мой прогресс", "Все показатели хранятся только на этом устройстве") }
        item {
            Tile {
                Text("ВЫПОЛНЕНО ДНЕЙ", color = Blue, fontSize = 11.sp)
                Text("$done / 84", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress = { done / 84f }, modifier = Modifier.fillMaxWidth(), color = Blue, trackColor = Panel2)
            }
        }
        item { Tile { Text("ТРЕНАЖЁР РЕШЕНИЙ", color = Blue, fontSize = 11.sp); Text("$attempts сценариев", fontSize = 26.sp, fontWeight = FontWeight.Bold); Text("Правильные решения: $correct (${if (attempts == 0) 0 else correct * 100 / attempts}%)", color = Muted) } }
        item { Tile { Text("ДЫХАТЕЛЬНЫЕ СЕССИИ", color = Blue, fontSize = 11.sp); Text("$breath", fontSize = 30.sp, fontWeight = FontWeight.Bold); Text("Полных трёхминутных сессий", color = Muted) } }
        item {
            Tile {
                Text("ТЕКУЩИЙ ЭТАП", color = Blue, fontSize = 11.sp)
                Text("Неделя $week — ${phase(week)}", fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(phaseInfo(week), color = Muted)
            }
        }
        item { Text("Время выбора в тренажёре служит ориентиром, а не оценкой реальной способности действовать при опасности.", color = Muted, fontSize = 12.sp) }
    }
}
