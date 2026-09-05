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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.R
import com.mi.bibliarv1960.ui.viewmodel.BibleViewModel

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

                // Lectura descontinua
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
                                    "Lectura descontinua",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "Organiza los versículos en párrafos independientes y muestra tus notas personales en color rojo.",
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    lineHeight = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Switch(
                                checked = isDiscontinuous,
                                onCheckedChange = { viewModel.toggleDiscontinuousMode() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }

                        AnimatedVisibility(
                            visible = isDiscontinuous,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) {
                            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(bottom = 16.dp),
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                Image(
                                    painter = painterResource(id = R.drawable.img_tutorial_discontinua),
                                    contentDescription = "Ejemplo de lectura descontinua",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.FillWidth
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Este modo facilita el estudio y la meditación al separar cada versículo y mostrar tus reflexiones directamente bajo el texto bíblico.",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                    lineHeight = 15.sp
                                )
                            }
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
