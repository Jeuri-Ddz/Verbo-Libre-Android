package com.mi.bibliarv1960.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mi.bibliarv1960.data.local.entities.BookmarkCategoryEntity
import com.mi.bibliarv1960.data.local.entities.BookmarkEntity
import com.mi.bibliarv1960.ui.theme.LinoIcons
import com.mi.bibliarv1960.ui.viewmodel.BibleViewModel
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.platform.LocalContext
import android.view.View
import android.app.Activity
import com.mi.bibliarv1960.R
import com.mi.bibliarv1960.ui.components.BookmarksOnboarding
import com.mi.bibliarv1960.utils.findActivity
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    viewModel: BibleViewModel,
    onOpenDrawer: () -> Unit,
    onBookmarkClick: (bookId: Int, chapter: Int, verse: Int) -> Unit
) {
    val bookmarks by viewModel.filteredBookmarks.collectAsState()
    val categories by viewModel.allCategories.collectAsState()
    val activeFilters by viewModel.activeCategoryFilters.collectAsState()

    val context = LocalContext.current
    val bookmarksOnboarding = remember { 
        val activity = context.findActivity()
        if (activity != null) BookmarksOnboarding(activity) else null
    }

    var categoriesAnchor by remember { mutableStateOf<View?>(null) }
    var editAnchor by remember { mutableStateOf<View?>(null) }
    var fabAnchor by remember { mutableStateOf<View?>(null) }

    LaunchedEffect(categoriesAnchor, editAnchor, fabAnchor) {
        if (categoriesAnchor != null && editAnchor != null && fabAnchor != null && bookmarksOnboarding != null && !bookmarksOnboarding.isShown()) {
            delay(600)
            bookmarksOnboarding.start(categoriesAnchor!!, editAnchor!!, fabAnchor!!)
        }
    }
    
    var isEditMode by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<BookmarkCategoryEntity?>(null) }
    var showAddCategory by remember { mutableStateOf(false) }
    var bookmarkToDelete by remember { mutableStateOf<BookmarkEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<BookmarkCategoryEntity?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        "Marcadores", 
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = LinoIcons.MenuAsymmetric,
                            contentDescription = "Menú",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    Box(contentAlignment = Alignment.Center) {
                        IconButton(onClick = { isEditMode = !isEditMode }) {
                            Icon(
                                imageVector = Icons.Default.Edit, 
                                contentDescription = "Editar categorías",
                                tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        AndroidView(
                            factory = { ctx ->
                                View(ctx).apply {
                                    visibility = View.INVISIBLE
                                    editAnchor = this
                                }
                            },
                            modifier = Modifier.size(1.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets(0.dp, 24.dp, 0.dp, 0.dp)
            )
        },
        floatingActionButton = {
            Box(contentAlignment = Alignment.Center) {
                FloatingActionButton(
                    onClick = { showAddCategory = true },
                    containerColor = Color(0xFF33506E),
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva categoría")
                }
                AndroidView(
                    factory = { ctx ->
                        View(ctx).apply {
                            visibility = View.INVISIBLE
                            fabAnchor = this
                        }
                    },
                    modifier = Modifier.size(1.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0.dp, 24.dp, 0.dp, 0.dp)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            // Category Filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(categories, key = { it.id }) { category ->
                    val currentFilters = activeFilters
                    val isActive = currentFilters == null || category.id in currentFilters
                    
                    Box {
                        CategoryChip(
                            category = category,
                            isActive = isActive,
                            isEditMode = isEditMode,
                            onClick = { viewModel.toggleCategoryFilter(category.id) },
                            onEditClick = { categoryToEdit = category }
                        )
                        
                        // Si es la primera categoría (generalmente Favoritos), ponemos el ancla
                        if (categories.indexOf(category) == 0) {
                            AndroidView(
                                factory = { ctx ->
                                    View(ctx).apply {
                                        visibility = View.INVISIBLE
                                        categoriesAnchor = this
                                    }
                                },
                                modifier = Modifier.size(1.dp).align(Alignment.Center)
                            )
                        }
                    }
                }
            }

            if (bookmarks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Aún no tienes versículos marcados.\nToca el número de un versículo mientras lees para guardarlo aquí.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 80.dp) // Gap for FAB
                ) {
                    items(bookmarks, key = { it.id }) { bookmark ->
                        val category = categories.find { it.id == bookmark.categoryId }
                        BookmarkRow(
                            bookmark = bookmark,
                            category = category,
                            onClick = { onBookmarkClick(bookmark.bookId, bookmark.chapter, bookmark.verse) },
                            onLongClick = { bookmarkToDelete = bookmark }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 22.dp),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }

    // Dialogs
    categoryToEdit?.let { category ->
        EditCategoryDialog(
            category = category,
            onDismiss = { categoryToEdit = null },
            onConfirm = { newName ->
                viewModel.renameCategory(category.id, newName)
                categoryToEdit = null
            },
            onDelete = {
                categoryToDelete = category
                categoryToEdit = null
            }
        )
    }

    categoryToDelete?.let { category ->
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Eliminar categoría") },
            text = { 
                Text("¿Estás seguro de que quieres eliminar la categoría '${category.name}'?\n\nEsta acción borrará también todos los versículos marcados en ella.") 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.removeCategory(category.id)
                        categoryToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    bookmarkToDelete?.let { bookmark ->
        AlertDialog(
            onDismissRequest = { bookmarkToDelete = null },
            title = { Text("Eliminar marcador") },
            text = { Text("¿Deseas eliminar este versículo de tus marcadores?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBookmark(bookmark)
                        bookmarkToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { bookmarkToDelete = null }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showAddCategory) {
        AddCategoryDialog(
            onDismiss = { showAddCategory = false },
            onConfirm = { name, colorHex ->
                viewModel.addCategory(name, colorHex)
                showAddCategory = false
            }
        )
    }
}

@Composable
fun CategoryChip(
    category: BookmarkCategoryEntity,
    isActive: Boolean,
    isEditMode: Boolean,
    onClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val color = Color(android.graphics.Color.parseColor(category.colorHex))
    val backgroundColor = if (isActive) color.copy(alpha = 0.14f) else Color.Transparent
    val borderColor = if (isActive) color.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = category.name,
            style = MaterialTheme.typography.labelLarge,
            color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (isEditMode) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onEditClick),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BookmarkRow(
    bookmark: com.mi.bibliarv1960.data.local.entities.BookmarkEntity,
    category: BookmarkCategoryEntity?,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val categoryColor = category?.let { Color(android.graphics.Color.parseColor(it.colorHex)) } ?: MaterialTheme.colorScheme.primary
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 22.dp, vertical = 16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Color bar indicator
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(40.dp)
                .background(categoryColor, RoundedCornerShape(2.dp))
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "${bookmark.bookName} ${bookmark.chapter}:${bookmark.verse}${if (category != null) " · ${category.name}" else ""}",
                style = MaterialTheme.typography.titleSmall,
                color = categoryColor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = bookmark.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun EditCategoryDialog(
    category: BookmarkCategoryEntity,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    onDelete: () -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Renombrar categoría") },
        text = {
            Column {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (category.id > 5) { // Evitar borrar las 5 categorías por defecto
                    Spacer(modifier = Modifier.height(16.dp))
                    TextButton(
                        onClick = onDelete,
                        modifier = Modifier.align(Alignment.End),
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Eliminar categoría")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name) }) {
                Text("Guardar")
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
fun AddCategoryDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    val colors = listOf("#E4574C", "#E3A711", "#3C9D6B", "#4C7FB0", "#8B5CF6", "#EC4899", "#0EA5A5", "#78716C")
    var selectedColor by remember { mutableStateOf(colors[0]) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nueva categoría") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true
                )
                Text("Color", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    colors.forEach { colorHex ->
                        val color = Color(android.graphics.Color.parseColor(colorHex))
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color, CircleShape)
                                .border(
                                    width = if (selectedColor == colorHex) 2.dp else 0.dp,
                                    color = if (selectedColor == colorHex) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (name.isNotBlank()) onConfirm(name, selectedColor) }) {
                Text("Crear")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
