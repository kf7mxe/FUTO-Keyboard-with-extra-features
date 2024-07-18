package org.futo.inputmethod.latin.uix.settings

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.preference.PreferenceManager
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.futo.inputmethod.latin.R
import org.futo.inputmethod.latin.uix.SettingsKey
import org.futo.inputmethod.latin.uix.getSettingBlocking
import org.futo.inputmethod.latin.uix.theme.Typography
import java.io.File
import java.io.FileOutputStream
import kotlin.math.pow


@Composable
fun ScreenTitle(title: String, showBack: Boolean = false, navController: NavHostController = rememberNavController()) {
    val rowModifier = if(showBack) {
        Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Navigate back") {
                navController.navigateUp()
            }
    } else {
        Modifier.fillMaxWidth()
    }
    Row(modifier = rowModifier) {
        Spacer(modifier = Modifier.width(16.dp))

        if(showBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.align(CenterVertically))
            Spacer(modifier = Modifier.width(18.dp))
        }
        Text(title, style = Typography.titleLarge, modifier = Modifier
            .align(CenterVertically)
            .padding(0.dp, 16.dp))
    }
}

@Composable
fun ScreenTitleWithIcon(title: String, painter: Painter) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Spacer(modifier = Modifier.width(16.dp))

        Icon(painter, contentDescription = null, modifier = Modifier.align(CenterVertically))
        Spacer(modifier = Modifier.width(18.dp))
        Text(title, style = Typography.titleLarge, modifier = Modifier
            .align(CenterVertically)
            .padding(0.dp, 16.dp))
    }
}

@Composable
@Preview
fun Tip(text: String = "This is an example tip") {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp), shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text,
            modifier = Modifier.padding(8.dp),
            style = Typography.bodyMedium,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}


@Composable
fun SettingItem(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    icon: (@Composable () -> Unit)? = null,
    disabled: Boolean = false,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(0.dp, 68.dp)
            .clickable(enabled = !disabled && onClick != null, onClick = {
                if (!disabled && onClick != null) {
                    onClick()
                }
            })
            .padding(0.dp, 4.dp, 0.dp, 4.dp)
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier
                .width(48.dp)
                .align(Alignment.CenterVertically)
        ) {
            Box(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                if (icon != null) {
                    icon()
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically)
                .alpha(
                    if (disabled) {
                        0.5f
                    } else {
                        1.0f
                    }
                )
        ) {
            Column {
                Text(title, style = Typography.bodyLarge)

                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = Typography.bodySmall,
                        color = LocalContentColor.current.copy(alpha = 0.5f)
                    )
                }
            }
        }
        Box(modifier = Modifier.align(Alignment.CenterVertically)) {
            content()
        }

        Spacer(modifier = Modifier.width(12.dp))
    }
}

@Composable
fun SettingToggleRaw(
    title: String,
    enabled: Boolean,
    setValue: (Boolean) -> Unit,
    subtitle: String? = null,
    disabled: Boolean = false,
    icon: (@Composable () -> Unit)? = null
) {
    SettingItem(
        title = title,
        subtitle = subtitle,
        onClick = {
            if (!disabled) {
                setValue(!enabled)
            }
        },
        icon = icon,
        modifier = Modifier.clearAndSetSemantics {
            this.text = AnnotatedString("$title. $subtitle")
            this.role = Role.Switch
            this.toggleableState = ToggleableState(enabled)
        }
    ) {
        Switch(checked = enabled, onCheckedChange = {
            if (!disabled) {
                setValue(!enabled)
            }
        }, enabled = !disabled)
    }
}

@Composable
fun SettingToggleDataStoreItem(
    title: String,
    dataStoreItem: DataStoreItem<Boolean>,
    subtitle: String? = null,
    disabledSubtitle: String? = null,
    disabled: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
) {
    val (enabled, setValue) = dataStoreItem

    val subtitleValue = if (!enabled && disabledSubtitle != null) {
        disabledSubtitle
    } else {
        subtitle
    }

    SettingToggleRaw(title, enabled, { setValue(it) }, subtitleValue, disabled, icon)
}

@Composable
fun SettingToggleDataStore(
    title: String,
    setting: SettingsKey<Boolean>,
    subtitle: String? = null,
    disabledSubtitle: String? = null,
    disabled: Boolean = false,
    icon: (@Composable () -> Unit)? = null
) {
    SettingToggleDataStoreItem(
        title, useDataStore(setting.key, setting.default), subtitle, disabledSubtitle, disabled, icon)
}

@Composable
fun SettingToggleSharedPrefs(
    title: String,
    key: String,
    default: Boolean,
    subtitle: String? = null,
    disabledSubtitle: String? = null,
    disabled: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
    additionalSettings: (@Composable () -> Unit)? = null
) {
    val (enabled, setValue) = useSharedPrefsBool(key, default)
    Column {
    SettingToggleDataStoreItem(
        title, useSharedPrefsBool(key, default), subtitle, disabledSubtitle, disabled, icon)
        if (enabled) {
            if (additionalSettings != null) {
                additionalSettings()
            }
        }
        }
}

@Composable
fun<T> SettingRadio(
    title: String,
    options: List<T>,
    optionNames: List<String>,
    setting: SettingsKey<T>,
) {
    val (value, setValue) = useDataStore(key = setting.key, default = setting.default)

    ScreenTitle(title, showBack = false)
    Column {
        options.zip(optionNames).forEach {
            SettingItem(title = it.second, onClick = { setValue(it.first) }, icon = {
                RadioButton(selected = value == it.first, onClick = null)
            }) {
                
            }
        }
    }
}

@Composable
private fun<T: Number> SettingSliderForDataStoreItem(
    title: String,
    item: DataStoreItem<T>,
    default: T,
    range: ClosedFloatingPointRange<Float>,
    transform: (Float) -> T,
    indicator: (T) -> String = { it.toString() },
    hardRange: ClosedFloatingPointRange<Float> = range,
    power: Float = 1.0f,
    subtitle: String? = null,
    steps: Int = 0,
) {
    val context = LocalContext.current

    val (value, setValue) = item
    var virtualValue by remember { mutableFloatStateOf(value.toFloat().let {
        if(it == Float.POSITIVE_INFINITY || it == Float.NEGATIVE_INFINITY) {
            it
        } else {
            it.pow(1.0f / power)
        }
    }) }
    var isTextFieldVisible by remember { mutableStateOf(false) }
    var hasTextFieldFocusedYet by remember { mutableStateOf(false) }
    var textFieldValue by remember(value) {
        val s = value.toString()
        mutableStateOf(TextFieldValue(
            s,
            selection = TextRange(0, s.length)
        ))
    }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(isTextFieldVisible) {
        if(isTextFieldVisible) focusRequester.requestFocus()
    }

    Column {
        ScreenTitle(title, showBack = false)
        if(subtitle != null) {
            Text(subtitle, style = Typography.bodyMedium, modifier = Modifier.padding(12.dp, 0.dp))
        }
        Row(modifier = Modifier.padding(16.dp, 0.dp)) {
            if (isTextFieldVisible) {
                val apply = {
                    if(isTextFieldVisible) {
                        val number = textFieldValue.text.trim().toFloatOrNull()
                        val newValue = if (number != null) {
                            transform(number.coerceIn(hardRange))
                        } else {
                            default
                        }

                        setValue(newValue)
                        virtualValue = newValue.toFloat().pow(1.0f / power)

                        isTextFieldVisible = false
                        textFieldValue = TextFieldValue()
                    }
                }

                BasicTextField(
                    value = textFieldValue,
                    onValueChange = { textFieldValue = it },
                    modifier = Modifier
                        .weight(0.33f)
                        .align(CenterVertically)
                        .focusRequester(focusRequester)
                        .onFocusChanged {
                            if (it.isFocused) hasTextFieldFocusedYet = true
                            else if (!it.isFocused && hasTextFieldFocusedYet) apply()
                        },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            apply()
                        }
                    ),
                    singleLine = true,
                    textStyle = Typography.labelMedium.copy(color = MaterialTheme.colorScheme.onBackground),
                    cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                )
            } else {
                Text(
                    text = indicator(value),
                    modifier = Modifier
                        .weight(0.33f)
                        .align(Alignment.CenterVertically)
                        .clickable {
                            hasTextFieldFocusedYet = false
                            isTextFieldVisible = true
                        },
                    style = Typography.labelMedium
                )
            }
            Slider(
                value = virtualValue,
                onValueChange = {
                    virtualValue = it
                    setValue(transform(it.pow(power))) },
                valueRange = range.start.pow(1.0f / power) .. range.endInclusive.pow(1.0f / power),
                enabled = !isTextFieldVisible,
                modifier = Modifier.weight(1.0f),
                steps = steps
            )
        }
    }
}

@Composable
fun SoundSettingsSection(){

    val context = LocalContext.current
    val currentSoundDeleteSound = useSharedPrefsString("delete_sound", "default")
    val currentSoundEnterSound = useSharedPrefsString("enter_sound", "default")
    val currentSoundSpaceSound = useSharedPrefsString("space_sound", "default")
    val currentSoundKeySound = useSharedPrefsString("key_sound", "default")
    // align column to right
    Column(modifier = Modifier.padding(96.dp, 0.dp, 0.dp, 16.dp)) {
        SoundSettingItem(
            title = "Delete Sound",
            key = "delete_sound",
            currentSounds = currentSoundDeleteSound,
        )

        SoundSettingItem(
            title = "Enter Sound",
            key = "enter_sound",
            currentSounds = currentSoundEnterSound,
        )

        SoundSettingItem(
            title = "Space Sound",
            key = "space_sound",
            currentSounds = currentSoundSpaceSound,
        )

        SoundSettingItem(
            title = "Key Sound",
            key = "key_sound",
            currentSounds = currentSoundKeySound,
        )
    }
}

fun saveSoundPath(context: Context, key: String, paths: String) {
    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    val currentPath = sharedPrefs.getString(key, null)
    sharedPrefs.edit {
        putString(key, paths)
    }
}

fun loadSoundUri(context: Context, key: String): Uri? {
    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    val uriString = sharedPrefs.getString(key, null)
    return uriString?.let { Uri.parse(it) }
}


fun copyFileToInternalStorage(context: Context, uri: Uri): String {
    val inputStream = context.contentResolver.openInputStream(uri)
    val fileName = "${getFileName(context,uri)}" // Use appropriate file extension
    val file = File(context.filesDir, fileName)

    inputStream.use { input ->
        FileOutputStream(file).use { output ->
            input?.copyTo(output)
        }
    }
    return file.absolutePath
}

fun loadSoundPaths(context: Context, key: String): List<String>? {
    val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
    val value = sharedPrefs.getString(key, null)
    val parsedValue = value?.split(",")
    return parsedValue
}

@SuppressLint("Range")
fun getFileName(context:Context, uri: Uri): String? {
    var result: String? = null
    if (uri.scheme == "content") {
        val cursor: Cursor? = context.contentResolver.query(uri, null, null, null, null)
        try {
            if (cursor != null && cursor.moveToFirst() && cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) > 0) {
                result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
        } finally {
            if (cursor != null) {
                cursor.close()
            }
        }
    }
    if (result == null) {
        result = uri.path
        val cut = result!!.lastIndexOf('/')
        if (cut != -1) {
            result = result.substring(cut + 1)
        }
    }
    return result
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoundSettingItem(
    title: String,
    key: String,
    currentSounds: DataStoreItem<String>?,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val newFilePath = copyFileToInternalStorage(context, it)
            val parsedCurrentSounds = currentSounds?.value?.split(",")
            val paths = parsedCurrentSounds?.plus(newFilePath) ?: listOf(newFilePath)
            saveSoundPath(context, key, paths.toString())
        }
    }

    Text(text = title, style = MaterialTheme.typography.titleMedium)
    FlowRow( modifier = Modifier.fillMaxWidth() )
    {
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = "Current Sound: ${
                currentSounds.let {
                    if (it == null) {
                        "default"
                    } else {
                        ""
                    }
                }
            }", style = MaterialTheme.typography.bodyMedium
        )
        if (currentSounds != null) {
            val currentSoundParsedStringArray: List<String> = currentSounds.value.split(",")
                currentSoundParsedStringArray.mapIndexed { index, currentSound ->
                    InputChip(
                        onClick = {
                            // remove current sound from list
                            val newCurrentSounds = currentSounds.value.split(",").minus(currentSound) ?: listOf()
                            currentSounds.setValue(newCurrentSounds.toString())
                        },
                        label = { Text(currentSound.substringAfterLast("/") ?: "default") },
                        selected = currentSound.isNotEmpty(),
                        trailingIcon = {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                Modifier.size(InputChipDefaults.AvatarSize)
                            )
                        }
                    )
                }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Button(onClick = { launcher.launch("audio/*") }) {
            Icon(painterResource((R.drawable.attach_file)), "Copy")
        }
    }
}



@Composable
fun<T: Number> SettingSlider(
    title: String,
    setting: SettingsKey<T>,
    range: ClosedFloatingPointRange<Float>,
    transform: (Float) -> T,
    indicator: (T) -> String = { it.toString() },
    hardRange: ClosedFloatingPointRange<Float> = range,
    power: Float = 1.0f,
    subtitle: String? = null,
    steps: Int = 0
) {
    SettingSliderForDataStoreItem(
        title = title,
        item = useDataStore(setting, blocking = true),
        default = setting.default,
        range = range,
        transform = transform,
        indicator = indicator,
        hardRange = hardRange,
        power = power,
        subtitle = subtitle,
        steps = steps
    )
}

@Composable
fun SettingSliderSharedPrefsInt(
    title: String,
    key: String,
    default: Int,
    range: ClosedFloatingPointRange<Float>,
    transform: (Float) -> Int,
    indicator: (Int) -> String = { it.toString() },
    hardRange: ClosedFloatingPointRange<Float> = range,
    power: Float = 1.0f,
    subtitle: String? = null,
    steps: Int = 0
) {
    SettingSliderForDataStoreItem(
        title = title,
        item = useSharedPrefsInt(key, default),
        default = default,
        range = range,
        transform = transform,
        indicator = indicator,
        hardRange = hardRange,
        power = power,
        subtitle = subtitle,
        steps = steps
    )
}

@Composable
fun ScrollableList(content: @Composable () -> Unit) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        content()
    }
}

@Composable
fun SettingListLazy(content: LazyListScope.() -> Unit) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        content()
    }
}


enum class NavigationItemStyle {
    HomePrimary,
    HomeSecondary,
    HomeTertiary,
    MiscNoArrow,
    Misc,
    Mail
}

@Composable
fun NavigationItem(title: String, style: NavigationItemStyle, navigate: () -> Unit, icon: Painter? = null, subtitle: String? = null) {
    SettingItem(
        title = title,
        subtitle = subtitle,
        onClick = navigate,
        icon = {
            icon?.let {
                val circleColor = when(style) {
                    NavigationItemStyle.HomePrimary -> MaterialTheme.colorScheme.primaryContainer
                    NavigationItemStyle.HomeSecondary -> MaterialTheme.colorScheme.secondaryContainer
                    NavigationItemStyle.HomeTertiary -> MaterialTheme.colorScheme.tertiaryContainer

                    NavigationItemStyle.MiscNoArrow,
                    NavigationItemStyle.Misc,
                    NavigationItemStyle.Mail -> Color.Transparent
                }

                val iconColor = when(style) {
                    NavigationItemStyle.HomePrimary -> MaterialTheme.colorScheme.onPrimaryContainer
                    NavigationItemStyle.HomeSecondary -> MaterialTheme.colorScheme.onSecondaryContainer
                    NavigationItemStyle.HomeTertiary -> MaterialTheme.colorScheme.onTertiaryContainer

                    NavigationItemStyle.MiscNoArrow,
                    NavigationItemStyle.Mail,
                    NavigationItemStyle.Misc -> MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                }

                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(circleColor, this.size.maxDimension / 2.4f)
                    translate(
                        left = this.size.width / 2.0f - icon.intrinsicSize.width / 2.0f,
                        top = this.size.height / 2.0f - icon.intrinsicSize.height / 2.0f
                    ) {
                        with(icon) {
                            draw(icon.intrinsicSize, colorFilter = ColorFilter.tint(iconColor))
                        }
                    }
                }
            }
        }
    ) {
        when(style) {
            NavigationItemStyle.Misc -> Icon(Icons.Default.ArrowForward, contentDescription = null)
            NavigationItemStyle.Mail -> Icon(Icons.Default.Send, contentDescription = null)
            else -> {}
        }
    }
}

@Composable
fun SettingTextField(title: String, placeholder: String, field: SettingsKey<String>) {
    val context = LocalContext.current

    val personalDict = useDataStore(field)
    val textFieldValue = remember { mutableStateOf(context.getSettingBlocking(
        field.key, field.default)) }

    LaunchedEffect(textFieldValue.value) {
        personalDict.setValue(textFieldValue.value)
    }

    ScreenTitle(title)

    TextField(
        value = textFieldValue.value,
        onValueChange = {
            textFieldValue.value = it
        },
        placeholder = { Text(placeholder) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp, 4.dp),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun<T> DropDownPicker(
    label: String,
    options: List<T>,
    selection: T?,
    onSet: (T) -> Unit,
    getDisplayName: (T) -> String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        },
        modifier = modifier
    ) {
        TextField(
            readOnly = true,
            value = selection?.let(getDisplayName) ?: "None",
            onValueChange = { },
            label = if (label.isNotBlank()) {
                { Text(label) }
            } else {
                null
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            },
            colors = ExposedDropdownMenuDefaults.textFieldColors(
                focusedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedIndicatorColor = MaterialTheme.colorScheme.onPrimaryContainer,
                focusedTrailingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = {
                        Text(getDisplayName(selectionOption))
                    },
                    onClick = {
                        onSet(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}