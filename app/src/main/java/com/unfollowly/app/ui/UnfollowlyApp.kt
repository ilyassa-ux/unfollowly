package com.unfollowly.app.ui

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.unfollowly.app.model.Insights
import java.text.DateFormat
import java.util.Date

private enum class Tab(val label: String) { Home("Home"), People("People"), History("History"), Settings("Settings") }
private enum class ListType(val title: String) { Unfollowers("Unfollowers"), NotBack("Not following back"), Fans("Fans"), Mutuals("Mutuals") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable fun UnfollowlyApp(vm: AppViewModel) {
    val state by vm.state
    var tab by remember { mutableStateOf(Tab.Home) }
    var listType by remember { mutableStateOf<ListType?>(null) }
    val importer = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { it?.let(vm::import) }
    val importAction = { importer.launch(arrayOf("application/zip", "application/json", "application/octet-stream")) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { TopAppBar(title = { Text(if (listType == null) "Unfollowly" else listType!!.title, fontWeight = FontWeight.Bold) }, navigationIcon = { if (listType != null) TextButton(onClick = { listType = null }) { Text("‹ Back") } }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)) },
        bottomBar = { if (listType == null) NavigationBar(containerColor = MaterialTheme.colorScheme.surface) { Tab.entries.forEach { item -> NavigationBarItem(selected = tab == item, onClick = { tab = item }, icon = { Text(item.label.take(1), fontWeight = FontWeight.Black) }, label = { Text(item.label) }) } } },
        snackbarHost = { state.message?.let { Snackbar(modifier = Modifier.padding(12.dp), action = { TextButton(onClick = vm::dismissMessage) { Text("OK") } }) { Text(it) } } }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (listType != null) PeopleList(listType!!, state.insights) else when (tab) {
                Tab.Home -> Home(state, importAction) { listType = it }
                Tab.People -> PeopleHub(state.insights) { listType = it }
                Tab.History -> History(state)
                Tab.Settings -> Settings(vm)
            }
        }
    }
}

@Composable private fun Home(state: UiState, importAction: () -> Unit, open: (ListType) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Know what changed.", fontSize = 30.sp, fontWeight = FontWeight.Black)
            Text("Private Instagram insights, calculated only on your phone.", color = Color(0xFFABA6B8))
        }
        if (state.latest == null) item { EmptyImport(importAction) }
        else {
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BigMetric("Followers", state.latest.followers.size, Modifier.weight(1f))
                    BigMetric("Following", state.latest.following.size, Modifier.weight(1f))
                }
            }
            item {
                Text(if (state.previous == null) "Your first snapshot" else "Since your last import", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ChangeChip("+${state.insights.newFollowers.size} new", Color(0xFF55D6BE))
                    ChangeChip("−${state.insights.unfollowers.size} left", Color(0xFFFF6B82))
                }
            }
            item { InsightCard("Unfollowers", "People who left since the previous import", state.insights.unfollowers.size, Color(0xFFFF6B82)) { open(ListType.Unfollowers) } }
            item { InsightCard("Not following back", "You follow them, they don’t follow you", state.insights.notFollowingBack.size, Color(0xFFFFC857)) { open(ListType.NotBack) } }
            item { InsightCard("Fans", "They follow you, you don’t follow them", state.insights.fans.size, Color(0xFF55D6BE)) { open(ListType.Fans) } }
            item { Button(onClick = importAction, enabled = !state.importing, modifier = Modifier.fillMaxWidth().height(54.dp)) { Text(if (state.importing) "Importing…" else "Import a new snapshot") } }
        }
    }
}

@Composable private fun EmptyImport(importAction: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(24.dp)) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("Your account stays yours", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            Text("Download your follower information from Instagram as JSON, then import the ZIP. We never ask for your password or upload your data.", color = Color(0xFFBCB7C8))
            Button(onClick = importAction, modifier = Modifier.fillMaxWidth()) { Text("Choose Instagram export") }
            Text("Instagram → Accounts Center → Your information → Download your information", fontSize = 12.sp, color = Color(0xFF8E899B))
        }
    }
}

@Composable private fun BigMetric(label: String, value: Int, modifier: Modifier) = Card(modifier, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
    Column(Modifier.padding(18.dp)) { Text(value.toString(), fontSize = 28.sp, fontWeight = FontWeight.Black); Text(label, color = Color(0xFFAAA5B5)) }
}

@Composable private fun ChangeChip(text: String, color: Color) = Surface(color = color.copy(alpha = .13f), shape = RoundedCornerShape(50)) { Text(text, Modifier.padding(horizontal = 14.dp, vertical = 8.dp), color = color, fontWeight = FontWeight.Bold) }

@Composable private fun InsightCard(title: String, subtitle: String, count: Int, color: Color, onClick: () -> Unit) = Card(Modifier.fillMaxWidth().clickable(onClick = onClick), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(12.dp).background(color, RoundedCornerShape(50)))
        Column(Modifier.padding(horizontal = 14.dp).weight(1f)) { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 12.sp, color = Color(0xFF9E99AA)) }
        Text(count.toString(), fontSize = 22.sp, fontWeight = FontWeight.Black)
    }
}

@Composable private fun PeopleHub(insights: Insights, open: (ListType) -> Unit) = LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
    item { Text("People", fontSize = 28.sp, fontWeight = FontWeight.Black); Text("Clear lists. No fake “stalker” scores.", color = Color(0xFFAAA5B5)) }
    item { InsightCard("Unfollowers", "Since your last import", insights.unfollowers.size, Color(0xFFFF6B82)) { open(ListType.Unfollowers) } }
    item { InsightCard("Not following back", "Review your outgoing follows", insights.notFollowingBack.size, Color(0xFFFFC857)) { open(ListType.NotBack) } }
    item { InsightCard("Fans", "Followers you may want to follow", insights.fans.size, Color(0xFF55D6BE)) { open(ListType.Fans) } }
    item { InsightCard("Mutuals", "You follow each other", insights.mutuals.size, Color(0xFF9C7BFF)) { open(ListType.Mutuals) } }
}

@Composable private fun PeopleList(type: ListType, insights: Insights) {
    val context = LocalContext.current
    val source = when(type) { ListType.Unfollowers -> insights.unfollowers; ListType.NotBack -> insights.notFollowingBack; ListType.Fans -> insights.fans; ListType.Mutuals -> insights.mutuals }
    var query by remember { mutableStateOf("") }
    val people = source.filter { it.contains(query, true) }.sorted()
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth(), placeholder = { Text("Search usernames") }, singleLine = true)
        Spacer(Modifier.height(10.dp))
        if (people.isEmpty()) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(if (source.isEmpty()) "Nothing here yet" else "No matches", color = Color(0xFFAAA5B5)) }
        else LazyColumn { items(people, key = { it }) { username -> ListItem(headlineContent = { Text("@$username", fontWeight = FontWeight.SemiBold) }, trailingContent = { TextButton(onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://instagram.com/$username"))) }) { Text("View") } }, colors = ListItemDefaults.colors(containerColor = Color.Transparent)); HorizontalDivider(color = Color(0xFF292632)) } }
    }
}

@Composable private fun History(state: UiState) = LazyColumn(contentPadding = PaddingValues(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
    item { Text("Import history", fontSize = 28.sp, fontWeight = FontWeight.Black); Text("Up to 30 snapshots, stored on this device.", color = Color(0xFFAAA5B5)); Spacer(Modifier.height(8.dp)) }
    if (state.snapshots.isEmpty()) item { Text("No imports yet.") }
    items(state.snapshots) { snapshot -> Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), shape = RoundedCornerShape(18.dp)) { Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceBetween) { Column { Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(snapshot.createdAt)), fontWeight = FontWeight.Bold); Text("${snapshot.followers.size} followers", color = Color(0xFFAAA5B5)) }; Text("${snapshot.following.size}\nfollowing") } } }
}

@Composable private fun Settings(vm: AppViewModel) {
    var confirm by remember { mutableStateOf(false) }
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Black)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) { Column(Modifier.padding(18.dp)) { Text("Privacy by design", fontWeight = FontWeight.Bold); Text("No Instagram login. No analytics SDK. No cloud database. Your imported lists remain on this device.", color = Color(0xFFAAA5B5)) } }
        OutlinedButton(onClick = { confirm = true }, modifier = Modifier.fillMaxWidth()) { Text("Delete all local data", color = MaterialTheme.colorScheme.error) }
        Text("Unfollowly is independent and is not affiliated with Instagram or Meta.", fontSize = 12.sp, color = Color(0xFF777282))
    }
    if (confirm) AlertDialog(onDismissRequest = { confirm = false }, title = { Text("Delete everything?") }, text = { Text("All imported snapshots and insights will be permanently removed from this device.") }, confirmButton = { TextButton(onClick = { vm.clear(); confirm = false }) { Text("Delete") } }, dismissButton = { TextButton(onClick = { confirm = false }) { Text("Cancel") } })
}
