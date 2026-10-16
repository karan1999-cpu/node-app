package com.cybersync.nodd.ui.screens

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cybersync.nodd.R
import com.cybersync.nodd.data.Profile
import com.cybersync.nodd.ui.Eyebrow
import com.cybersync.nodd.ui.PrimaryButton

@Composable
fun EditCardScreen(initial: Profile, onSave: (Profile) -> Unit, onBack: () -> Unit) {
    var firstName by rememberSaveable { mutableStateOf(initial.firstName) }
    var lastName by rememberSaveable { mutableStateOf(initial.lastName) }
    var phone by rememberSaveable { mutableStateOf(initial.phone) }
    var email by rememberSaveable { mutableStateOf(initial.email) }
    var jobTitle by rememberSaveable { mutableStateOf(initial.jobTitle) }
    var company by rememberSaveable { mutableStateOf(initial.company) }
    var instagram by rememberSaveable { mutableStateOf(initial.instagram) }
    var linkedin by rememberSaveable { mutableStateOf(initial.linkedin) }
    var website by rememberSaveable { mutableStateOf(initial.website) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    fun save() {
        val draft = initial.copy(
            firstName = firstName, lastName = lastName, phone = phone, email = email,
            jobTitle = jobTitle, company = company, instagram = instagram, linkedin = linkedin, website = website,
        )
        error = when {
            draft.isBlank -> "Add your name so people know whose card it is."
            phone.isBlank() && email.isBlank() -> "Add a mobile number or an email so people can reach you."
            phone.isNotBlank() && !phone.trim().matches(Regex("""\+?[0-9 ()\-]{6,20}""")) ->
                "The mobile number can only have digits, spaces, dashes and a leading +."
            email.isNotBlank() && !email.trim().matches(Regex("""[^@\s]+@[^@\s]+\.[^@\s]+""")) ->
                "That email doesn't look complete. Check it has an @ and a domain."
            else -> null
        }
        if (error == null) onSave(draft)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(painterResource(R.drawable.ic_back), contentDescription = "Back")
            }
            Text("Your details", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = ::save) {
                Text("Save", style = MaterialTheme.typography.labelLarge)
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
        ) {
            Eyebrow("Name")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Input(firstName, { firstName = it }, "First name", modifier = Modifier.weight(1f), capitalization = KeyboardCapitalization.Words)
                Input(lastName, { lastName = it }, "Last name", modifier = Modifier.weight(1f), capitalization = KeyboardCapitalization.Words)
            }

            Eyebrow("Reach me", Modifier.padding(top = 8.dp))
            Input(phone, { phone = it }, "Mobile number", icon = R.drawable.ic_phone, keyboard = KeyboardType.Phone, hint = "Include the country code, like +91 98765 43210")
            Input(email, { email = it }, "Email", icon = R.drawable.ic_mail, keyboard = KeyboardType.Email)

            Eyebrow("Work", Modifier.padding(top = 8.dp))
            Input(jobTitle, { jobTitle = it }, "Job title", icon = R.drawable.ic_work, capitalization = KeyboardCapitalization.Words)
            Input(company, { company = it }, "Company or college", icon = R.drawable.ic_work, capitalization = KeyboardCapitalization.Words)

            Eyebrow("Links", Modifier.padding(top = 8.dp))
            Input(instagram, { instagram = it }, "Instagram", icon = R.drawable.ic_insta, keyboard = KeyboardType.Uri, hint = "Your handle, like @riya.runs")
            Input(linkedin, { linkedin = it }, "LinkedIn", icon = R.drawable.ic_linkedin, keyboard = KeyboardType.Uri, hint = "Profile link, or the part after linkedin.com/in/")
            Input(website, { website = it }, "Website", icon = R.drawable.ic_web, keyboard = KeyboardType.Uri, last = true)

            error?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
            }
            PrimaryButton("Save card", R.drawable.ic_check, ::save, Modifier.padding(top = 4.dp, bottom = 16.dp))
        }
    }
}

@Composable
private fun Input(
    value: String,
    onChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    @DrawableRes icon: Int? = null,
    keyboard: KeyboardType = KeyboardType.Text,
    capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
    hint: String? = null,
    last: Boolean = false,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        leadingIcon = icon?.let { { Icon(painterResource(it), contentDescription = null, modifier = Modifier.size(20.dp)) } },
        supportingText = hint?.let { { Text(it) } },
        keyboardOptions = KeyboardOptions(
            capitalization = capitalization,
            keyboardType = keyboard,
            imeAction = if (last) ImeAction.Done else ImeAction.Next,
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
        ),
        modifier = modifier,
    )
}
