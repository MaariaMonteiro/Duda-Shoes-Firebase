package com.example.dudashoes

import android.R.color.black
import android.R.id.underline
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import com.example.dudashoes.ui.theme.DudaShoesTheme

val Purple700 = Color(0xFF7B1FA2)
val Dark = Color(0xFF050000)
val LightGray = Color(0xFFF5F5F5)

data class Calcado(
    val id: String = "",
    val nome: String = "",
    val marca: String = "",
    val preco: Double = 0.0,
    val tamanho: String = "",
    val descricao: String = "",
    val imagemUrl: String = ""
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DudaShoesTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLogin = { userName -> navController.navigate("home/${userName}") },
                onRegisterClick = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterComplete = { navController.navigate("login") },
                onLoginClick = { navController.navigate("login") }
            )
        }
        composable(
            "home/{userName}",
            arguments = listOf(navArgument("userName") { type = NavType.StringType })
        ) { backStackEntry ->
            HomeScreen(
                userName = backStackEntry.arguments?.getString("userName") ?: "",
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home/{userName}") { inclusive = true }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String = "Usuário",
    onLogout: () -> Unit
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val db = Firebase.firestore
    val banco = remember { mutableStateListOf<Map<String, Any>>() }
    val calcados = remember { mutableStateListOf<Calcado>() }

    var mostrarRegistros by remember { mutableStateOf(false) }
    var mostrarCalcados by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Text(
                    text = "Menu",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark
                    ),
                    modifier = Modifier.padding(16.dp)
                )

                Divider()

                Text(
                    text = "Início",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { drawerState.close() }
                            // Volta para a tela inicial
                            mostrarRegistros = false
                            mostrarCalcados = false
                        }
                        .padding(16.dp),
                    color = Dark
                )
                Text(
                    text = "Listar Registros",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { drawerState.close() }
                            mostrarRegistros = true
                            mostrarCalcados = false
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val result = db.collection("banco").get().await()
                                    banco.clear()
                                    for (document in result) {
                                        val dados = document.data.toMutableMap()
                                        dados["id"] = document.id  // 👈 aqui adiciona o id do documento
                                        banco.add(dados)
                                    }

                                } catch (e: Exception) {
                                    Log.e("Firestore", "Erro ao listar documentos", e)
                                }
                            }
                        }
                        .padding(16.dp),
                    color = Dark
                )

                Text(
                    text = "Listar Calçados",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { drawerState.close() }
                            mostrarCalcados = true
                            mostrarRegistros = false
                            carregarCalcados(db, calcados)
                        }
                        .padding(16.dp),
                    color = Dark
                )

                Text(
                    text = "Sair",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            scope.launch { drawerState.close() }
                            onLogout()
                        }
                        .padding(16.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Duda Shoes",
                            color = Dark,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch {
                                if (drawerState.isClosed) drawerState.open()
                                else drawerState.close()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Dark
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when {
                    mostrarRegistros -> {
                        Text(
                            text = "Role para baixo ↓",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 19.sp,
                                color = Dark.copy(alpha = 0.9f),
                                fontStyle = FontStyle.Italic
                            ),
                            modifier = Modifier
                                .padding(top = 4.dp, bottom = 4.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                        ListaRegistrosScreen(banco, db)
                    }

                    mostrarCalcados -> {
                        Text(
                            text = "Calçados Disponíveis",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Dark,
                                        textDecoration = TextDecoration.Underline

                            ),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        ListaCalcadosScreen(calcados, db, onCalcadosUpdated = {
                            carregarCalcados(db, calcados)
                        })
                    }

                    else -> {
                        // 👇 Só aparece na Home
                        Image(
                            painter = painterResource(id = R.drawable.user),
                            contentDescription = "Logo",
                            modifier = Modifier.size(160.dp),
                            contentScale = ContentScale.Fit
                        )

                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Bem-vindo à Duda Shoes, $userName!",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Dark
                            ),
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Use o menu para listar calçados ou registros",
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(27.dp))
                    }
                }
            }
        }}}



        @Composable
fun ListaCalcadosScreen(
    calcados: List<Calcado>,
    db: com.google.firebase.firestore.FirebaseFirestore,
    onCalcadosUpdated: () -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Button(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Dark)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Adicionar")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Adicionar Calçado",

            )
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(calcados) { calcado ->
                CalcadoCard(
                    calcado = calcado,
                    onEdit = { updatedCalcado ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                db.collection("calcados").document(updatedCalcado.id)
                                    .set(updatedCalcado)
                                    .await()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Calçado atualizado!", Toast.LENGTH_SHORT).show()
                                    onCalcadosUpdated()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Erro ao atualizar: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    onDelete = { calcadoId ->
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                db.collection("calcados").document(calcadoId)
                                    .delete()
                                    .await()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Calçado excluído!", Toast.LENGTH_SHORT).show()
                                    onCalcadosUpdated()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Erro ao excluir: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                )
            }
        }
    }

    if (showAddDialog) {
        AddEditCalcadoDialog(
            calcado = null,
            onSave = { novoCalcado ->
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val docRef = db.collection("calcados").document()
                        val calcadoComId = novoCalcado.copy(id = docRef.id)
                        docRef.set(calcadoComId).await()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Calçado adicionado!", Toast.LENGTH_SHORT).show()
                            onCalcadosUpdated()
                            showAddDialog = false
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "Erro ao adicionar: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onDismiss = { showAddDialog = false }
        )
    }
}

@Composable
fun CalcadoCard(
    calcado: Calcado,
    onEdit: (Calcado) -> Unit,
    onDelete: (String) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = LightGray
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.tenis_placeholder),
                    contentDescription = "Calçado",
                    modifier = Modifier.size(120.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = calcado.nome,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Dark
                )
            )

            Text(
                text = calcado.marca,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = calcado.descricao,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Tamanho: ${calcado.tamanho}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    )
                )

                Text(
                    text = "R$ ${"%.2f".format(calcado.preco)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Dark
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showEditDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Dark),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Editar")
                }

                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Excluir")
                }
            }
        }
    }

    if (showEditDialog) {
        AddEditCalcadoDialog(
            calcado = calcado,
            onSave = { updatedCalcado ->
                onEdit(updatedCalcado)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Confirmar Exclusão") },
            text = { Text("Deseja realmente excluir o calçado \"${calcado.nome}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(calcado.id)
                        showDeleteDialog = false
                    }
                ) {
                    Text("Sim", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun AddEditCalcadoDialog(
    calcado: Calcado?,
    onSave: (Calcado) -> Unit,
    onDismiss: () -> Unit
) {
    var nome by remember { mutableStateOf(calcado?.nome ?: "") }
    var marca by remember { mutableStateOf(calcado?.marca ?: "") }
    var preco by remember { mutableStateOf(calcado?.preco?.toString() ?: "") }
    var tamanho by remember { mutableStateOf(calcado?.tamanho ?: "") }
    var descricao by remember { mutableStateOf(calcado?.descricao ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (calcado == null) "Adicionar Calçado" else "Editar Calçado") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                CustomTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = "Nome do calçado",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = marca,
                    onValueChange = { marca = it },
                    label = "Marca",
                    modifier = Modifier.fillMaxWidth()
                )


                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = preco,
                    onValueChange = { preco = it },
                    label = "Preço (R$)",
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = tamanho,
                    onValueChange = { tamanho = it },
                    label = "Tamanho",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                CustomTextField(
                    value = descricao,
                    onValueChange = { descricao = it },
                    label = "Descrição",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (nome.isNotBlank() && marca.isNotBlank() && preco.isNotBlank() && tamanho.isNotBlank()) {
                        val calcadoAtualizado = Calcado(
                            id = calcado?.id ?: "",
                            nome = nome,
                            marca = marca,
                            preco = preco.toDoubleOrNull() ?: 0.0,
                            tamanho = tamanho,
                            descricao = descricao
                        )
                        onSave(calcadoAtualizado)
                    }
                }
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
@Composable
fun ListaRegistrosScreen(
    banco: List<Map<String, Any>>,
    db: com.google.firebase.firestore.FirebaseFirestore
) {
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            Text(
                text = "Registros Cadastrados:",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Dark,
                    textDecoration = TextDecoration.Underline
                ),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                banco.forEachIndexed { index, registro ->
                    var showEditDialog by remember { mutableStateOf(false) }
                    var showDeleteDialog by remember { mutableStateOf(false) }

                    // Estados locais dos campos de edição
                    var nomeEdit by remember { mutableStateOf(registro["nome"].toString()) }
                    var apelidoEdit by remember { mutableStateOf(registro["apelido"].toString()) }
                    var emailEdit by remember { mutableStateOf(registro["email"].toString()) }
                    var telefoneEdit by remember { mutableStateOf(registro["telefone"].toString()) }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Registro ${index + 1}",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = Dark,
                                    fontWeight = FontWeight.Bold,
                                    textDecoration = TextDecoration.Underline
                                )
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Nome: ${registro["nome"]}")
                            Text("Apelido: ${registro["apelido"]}")
                            Text("Email: ${registro["email"]}")
                            Text("Telefone: ${registro["telefone"]}")
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Button(
                                    onClick = { showEditDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Dark),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Editar")
                                }

                                Button(
                                    onClick = { showDeleteDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Excluir")
                                }
                            }
                        }
                    }

                    // 🔹 Diálogo de Edição
                    if (showEditDialog) {
                        AlertDialog(
                            onDismissRequest = { showEditDialog = false },
                            confirmButton = {
                                Button(onClick = {
                                    val id = registro["id"] ?: return@Button
                                    val dadosAtualizados = mapOf(
                                        "nome" to nomeEdit,
                                        "apelido" to apelidoEdit,
                                        "email" to emailEdit,
                                        "telefone" to telefoneEdit
                                    )

                                    db.collection("banco")
                                        .document(id.toString())
                                        .update(dadosAtualizados)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Registro atualizado!", Toast.LENGTH_SHORT).show()
                                            showEditDialog = false
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Erro ao atualizar!", Toast.LENGTH_SHORT).show()
                                        }
                                }) {
                                    Text("Salvar")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showEditDialog = false }) {
                                    Text("Cancelar")
                                }
                            },
                            title = { Text("Editar Registro") },
                            text = {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(value = nomeEdit, onValueChange = { nomeEdit = it }, label = { Text("Nome") })
                                    OutlinedTextField(value = apelidoEdit, onValueChange = { apelidoEdit = it }, label = { Text("Apelido") })
                                    OutlinedTextField(value = emailEdit, onValueChange = { emailEdit = it }, label = { Text("E-mail") })
                                    OutlinedTextField(value = telefoneEdit, onValueChange = { telefoneEdit = it }, label = { Text("Telefone") })
                                }
                            }
                        )
                    }

                    // 🔹 Diálogo de Exclusão
                    if (showDeleteDialog) {
                        AlertDialog(
                            onDismissRequest = { showDeleteDialog = false },
                            confirmButton = {
                                Button(onClick = {
                                    val id = registro["id"] ?: return@Button
                                    db.collection("banco")
                                        .document(id.toString())
                                        .delete()
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Registro excluído!", Toast.LENGTH_SHORT).show()
                                            showDeleteDialog = false
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "Erro ao excluir!", Toast.LENGTH_SHORT).show()
                                        }
                                }) {
                                    Text("Confirmar")
                                }
                            },
                            dismissButton = {
                                Button(onClick = { showDeleteDialog = false }) {
                                    Text("Cancelar")
                                }
                            },
                            title = { Text("Excluir Registro") },
                            text = { Text("Tem certeza que deseja excluir este registro?") }
                        )
                    }
                }
            }
        }
    }
}


private fun carregarCalcados(
    db: com.google.firebase.firestore.FirebaseFirestore,
    calcados: MutableList<Calcado>
) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val result = db.collection("calcados").get().await()
            calcados.clear()
            for (document in result) {
                val calcado = document.toObject(Calcado::class.java)
                calcados.add(calcado.copy(id = document.id))
            }
        } catch (e: Exception) {
            Log.e("Firestore", "Erro ao carregar calçados", e)
        }
    }
}
@Composable
fun LoginScreen(
    onLogin: (String) -> Unit,
    onRegisterClick: () -> Unit
) {
    val context = LocalContext.current
    val db = Firebase.firestore
    val auth = Firebase.auth

    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F3)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // 🔹 Imagem fora do card, centralizada
            Image(
                painter = painterResource(id = R.drawable.login),
                contentDescription = "Login",
                modifier = Modifier
                    .size(130.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Fit
            )

            // 🔹 Card principal com fundo cinza discreto
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Acesse seu perfil:",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Dark,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = senha,
                        onValueChange = { senha = it },
                        label = { Text("Senha") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (email.isBlank() || senha.isBlank()) {
                                errorMessage = "Preencha todos os campos"
                                return@Button
                            }

                            auth.signInWithEmailAndPassword(email, senha)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            try {
                                                val query = db.collection("banco")
                                                    .whereEqualTo("email", email)
                                                    .get()
                                                    .await()

                                                val nomeUsuario = query.documents
                                                    .getOrNull(0)?.getString("apelido") ?: email

                                                withContext(Dispatchers.Main) {
                                                    onLogin(nomeUsuario)
                                                }
                                            } catch (e: Exception) {
                                                withContext(Dispatchers.Main) {
                                                    errorMessage = "Erro ao buscar dados: ${e.message}"
                                                }
                                            }
                                        }
                                    } else {
                                        errorMessage = "Credenciais inválidas"
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Dark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Entrar", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // 🔹 Botão "Criar Conta" fora do card
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onRegisterClick) {
                Text(
                    "Não tem conta? Cadastre-se",
                    color = Dark,
                    textDecoration = TextDecoration.Underline,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RegisterScreen(
    onRegisterComplete: () -> Unit,
    onLoginClick: () -> Unit
) {
    val context = LocalContext.current
    val db = Firebase.firestore

    var nome by remember { mutableStateOf("") }
    var apelido by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }
    var telefone by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F3F3)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            // 🔹 Imagem fora do card, centralizada
            Image(
                painter = painterResource(id = R.drawable.cadastro),
                contentDescription = "Cadastro",
                modifier = Modifier
                    .size(130.dp)
                    .padding(bottom = 8.dp),
                contentScale = ContentScale.Fit
            )

            // 🔹 Card principal com fundo cinza discreto
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE0E0E0)),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Crie sua conta",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Dark,
                            textDecoration = TextDecoration.Underline
                        ),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )

                    if (errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    // 🔹 Nome e Apelido - organizados responsivamente
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Primeiro Nome:",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Dark)
                            )
                            OutlinedTextField(
                                value = nome,
                                onValueChange = { nome = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Apelido:",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Dark)
                            )
                            OutlinedTextField(
                                value = apelido,
                                onValueChange = { apelido = it },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true
                            )
                        }
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("E-mail") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = senha,
                        onValueChange = { senha = it },
                        label = { Text("Senha") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = telefone,
                        onValueChange = { telefone = it },
                        label = { Text("Telefone") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )

                    Button(
                        onClick = {
                            if (nome.isBlank() || apelido.isBlank() || email.isBlank() || senha.isBlank()) {
                                errorMessage = "Preencha todos os campos obrigatórios!"
                                return@Button
                            }

                            val auth = Firebase.auth
                            auth.createUserWithEmailAndPassword(email, senha)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        CoroutineScope(Dispatchers.IO).launch {
                                            val usuario = hashMapOf(
                                                "nome" to nome,
                                                "apelido" to apelido,
                                                "email" to email,
                                                "telefone" to telefone
                                            )
                                            db.collection("banco").add(usuario).await()
                                            withContext(Dispatchers.Main) {
                                                onRegisterComplete()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Erro: ${task.exception?.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Dark),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cadastrar", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            // 🔹 Botão "Já tem uma conta? Faça login" fora do card
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onLoginClick) {
                Text(
                    "Já tem uma conta? Faça login",
                    color = Dark,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun CustomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    TextField(
        value = value,
        keyboardOptions = keyboardOptions,
                onValueChange = onValueChange,
        label = { Text(text = label) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
        ),
        shape = MaterialTheme.shapes.small,
        trailingIcon = trailingIcon,
        modifier = modifier,
        singleLine = singleLine
    )
}