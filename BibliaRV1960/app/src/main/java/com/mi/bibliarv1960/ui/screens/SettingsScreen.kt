package com.mi.bibliarv1960.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import android.content.Intent
import android.content.ContentValues
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.R
import com.mi.bibliarv1960.ui.viewmodel.BibleViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: BibleViewModel,
    onBack: () -> Unit
) {
    val isDiscontinuous by viewModel.isDiscontinuousMode.collectAsState()
    val fontSize by viewModel.fontSize.collectAsState()
    val fontFamilyName by viewModel.fontFamily.collectAsState()
    val selectedTranslationId by viewModel.selectedTranslationId.collectAsState()
    val ttsSpeed by viewModel.preferredTtsSpeed.collectAsState()
    val showTrivia by viewModel.showTrivia.collectAsState()
    val showDevotional by viewModel.showDevocional.collectAsState()
    val autoDnd by viewModel.autoDndOnReading.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    
    val context = LocalContext.current
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val json = inputStream.bufferedReader().use { reader -> reader.readText() }
                    viewModel.importData(json)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Error al abrir el archivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.exportEvent.collect { json ->
            val success = withContext(Dispatchers.IO) {
                try {
                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                    val fileName = "respaldo_verbo_libre_$timestamp.json"

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                        }

                        val resolver = context.contentResolver
                        val contentUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI
                        val uri = resolver.insert(contentUri, contentValues)

                        uri?.let {
                            resolver.openOutputStream(it)?.use { outputStream ->
                                outputStream.write(json.toByteArray())
                            }
                            true
                        } ?: false
                    } else {
                        // Fallback para versiones anteriores a Android 10
                        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                        if (!downloadsDir.exists()) downloadsDir.mkdirs()
                        val file = File(downloadsDir, fileName)
                        FileOutputStream(file).use { it.write(json.toByteArray()) }
                        true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }

            if (success) {
                Toast.makeText(context, "Respaldo guardado en la carpeta de Descargas", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error al guardar el respaldo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.messageEvent.collect { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
    }

    var showResetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            SettingsSection(title = "LECTURA") {
                // Traducción por defecto
                SettingsDropdown(
                    label = "Traducción por defecto",
                    currentValue = if (selectedTranslationId == "rv1909") "Reina Valera 1909" else "Versión Lenguaje Moderno",
                    options = listOf("Reina Valera 1909", "Versión Lenguaje Moderno"),
                    onOptionSelected = { option ->
                        viewModel.selectTranslation(if (option == "Reina Valera 1909") "rv1909" else "vbl")
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tipo de Fuente
                SettingsDropdown(
                    label = "Tipo de fuente",
                    currentValue = when(fontFamilyName) {
                        "SERIF" -> "Serif (Clásica)"
                        "MONOSPACE" -> "Moderna"
                        else -> "Sistema (Roboto)"
                    },
                    options = listOf("Sistema (Roboto)", "Serif (Clásica)", "Moderna"),
                    onOptionSelected = { option ->
                        val family = when(option) {
                            "Serif (Clásica)" -> "SERIF"
                            "Moderna" -> "MONOSPACE"
                            else -> "SANS_SERIF"
                        }
                        viewModel.setFontFamily(family)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Tamaño de Fuente
                SettingsCard {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tamaño de fuente", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Ajusta el tamaño del texto para la lectura.", fontSize = 12.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewModel.updateFontSize(-1f) }) {
                                Text("A-", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Text(
                                text = fontSize.toInt().toString(),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            IconButton(onClick = { viewModel.updateFontSize(1f) }) {
                                Text("A+", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Modo Lectura Continua
                Surface(
                    onClick = { viewModel.toggleDiscontinuousMode() },
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "Lectura continua",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Agrupa los versículos en bloques de texto fluido y oculta las notas del flujo principal.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Switch(
                                checked = !isDiscontinuous,
                                onCheckedChange = { viewModel.toggleDiscontinuousMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }

                        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                            HorizontalDivider(
                                modifier = Modifier.padding(bottom = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                            
                            // Vista previa interactiva
                            val primaryColor = MaterialTheme.colorScheme.primary
                            val noteTextColor = if (isDarkMode) Color(0xFFFF8A80) else Color(0xFFB8310F)
                            
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "VISTA PREVIA",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                                
                                if (isDiscontinuous) {
                                    // Modo Versículos (Default)
                                    Column {
                                        Text(buildAnnotatedString {
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 12.sp)) {
                                                append("1 ")
                                            }
                                            append("En el principio creó Dios los cielos y la tierra.")
                                        }, fontSize = 14.sp)
                                        
                                        Text(
                                            text = "Dios es el creador de todo lo que existe.",
                                            color = noteTextColor,
                                            fontSize = 13.sp,
                                            fontStyle = FontStyle.Italic,
                                            modifier = Modifier.padding(vertical = 4.dp)
                                        )
                                        
                                        Text(buildAnnotatedString {
                                            withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 12.sp)) {
                                                append("2 ")
                                            }
                                            append("Y la tierra estaba desordenada y vacía...")
                                        }, fontSize = 14.sp)
                                    }
                                } else {
                                    // Modo Continuo
                                    Text(buildAnnotatedString {
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 12.sp)) {
                                            append("1 ")
                                        }
                                        withStyle(SpanStyle(textDecoration = TextDecoration.Underline)) {
                                            append("En el principio creó Dios los cielos y la tierra.")
                                        }
                                        append(" ")
                                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = primaryColor, fontSize = 12.sp)) {
                                            append("2 ")
                                        }
                                        append("Y la tierra estaba desordenada y vacía, y las tinieblas estaban sobre la faz del abismo...")
                                    }, fontSize = 14.sp, lineHeight = 20.sp)
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (isDiscontinuous) 
                                    "Este modo facilita el estudio al mostrar tus notas directamente bajo cada versículo." 
                                    else "Este modo ofrece una experiencia de lectura fluida. Las notas se indican con un subrayado.",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            SettingsSection(title = "AUDIO (TTS)") {
                SettingsCard {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Velocidad de voz", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Configura la rapidez de la lectura por voz.", fontSize = 12.sp, color = Color.Gray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val speeds = listOf(0.75f, 1.0f, 1.25f, 1.5f)
                            speeds.forEach { speed ->
                                FilterChip(
                                    selected = ttsSpeed == speed,
                                    onClick = { viewModel.setPreferredTtsSpeed(speed) },
                                    label = { Text("${speed}x", fontSize = 10.sp) },
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            SettingsSection(title = "PERSONALIZACIÓN") {
                SettingsSwitch(
                    label = "Mostrar Reto del día",
                    description = "Activa o desactiva la trivia diaria en el inicio.",
                    checked = showTrivia,
                    onCheckedChange = { viewModel.setShowTrivia(it) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsSwitch(
                    label = "Mostrar Devocional",
                    description = "Muestra u oculta la meditación diaria en el inicio.",
                    checked = showDevotional,
                    onCheckedChange = { viewModel.setShowDevocional(it) }
                )
                Spacer(modifier = Modifier.height(12.dp))
                SettingsSwitch(
                    label = "Silenciar notificaciones al leer",
                    description = "Activa automáticamente el modo 'No molestar' mientras estés en el lector bíblico.",
                    checked = autoDnd,
                    onCheckedChange = { enabled ->
                        if (enabled && !viewModel.hasNotificationPolicyAccess()) {
                            // Abrir configuración si no tiene permiso
                            val intent = Intent(android.provider.Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            context.startActivity(intent)
                        } else {
                            viewModel.setAutoDndOnReading(enabled)
                        }
                    }
                )
            }

            SettingsSection(title = "DATOS Y RESPALDO") {
                SettingsCard(onClick = { viewModel.exportData() }) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Exportar mis datos", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Crea una copia de seguridad de tus notas y marcadores.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SettingsCard(onClick = { importLauncher.launch("application/json") }) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info, 
                            contentDescription = null, 
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Importar datos", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Restaura tus notas y marcadores desde un archivo de respaldo.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            SettingsSection(title = "AYUDA Y SOPORTE") {
                SettingsCard(onClick = { showResetDialog = true }) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Reiniciar guías de ayuda", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("Vuelve a ver los tutoriales de cada sección.", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Verbo Libre · v1.0",
                modifier = Modifier.align(Alignment.CenterHorizontally),
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reiniciar ayuda") },
            text = { Text("¿Estás seguro de que quieres volver a ver los tutoriales de ayuda en toda la aplicación?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetOnboarding()
                    showResetDialog = false
                }) {
                    Text("Reiniciar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(vertical = 12.dp),
            letterSpacing = 1.sp
        )
        content()
    }
}

@Composable
fun SettingsCard(onClick: (() -> Unit)? = null, content: @Composable ColumnScope.() -> Unit) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(content = content)
    }
}

@Composable
fun SettingsSwitch(label: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    SettingsCard {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(description, fontSize = 12.sp, color = Color.Gray, lineHeight = 16.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun SettingsDropdown(label: String, currentValue: String, options: List<String>, onOptionSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    SettingsCard(onClick = { expanded = true }) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(currentValue, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
