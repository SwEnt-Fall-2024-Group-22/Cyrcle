package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.TopLevelDestination

/**
 * Create a themed bottom navigation bar, with simplified arguments.
 *
 * @param onTabSelect A lambda to execute when any tab is clicked.
 * @param tabList The list of tab to display.
 * @param selectedItem The current selected tab, given by `Route.SCREEN_NAME`.
 * @param testTag The test tag of the object. Each item has its own tag : `tab.textId`.
 */
@Composable
fun BottomNavigationBar(
    navigationActions: NavigationActions,
    tabList: List<TopLevelDestination> = LIST_TOP_LEVEL_DESTINATION,
    onTabSelect: (TopLevelDestination) -> Unit = { navigationActions.navigateTo(it) },
    selectedItem: String,
    testTag: String = "BottomNavigationBar"
) {
  BottomNavigation(
      modifier = Modifier.testTag(testTag).fillMaxWidth().height(60.dp),
      backgroundColor = MaterialTheme.colorScheme.surface,
      content = {
        tabList.forEach { tab ->
          BottomNavigationItem(
              // OPTIMIZATION Find a way to add some contentDescription
              icon = {
                Icon(
                    tab.icon,
                    contentDescription = tab.textId,
                    Modifier.testTag("${tab.textId}Icon"))
              },
              label = { Text(tab.textId, Modifier.testTag("${tab.textId}Text")) },
              selected = tab.route == selectedItem,
              onClick = { onTabSelect(tab) },
              modifier = Modifier.clip(RoundedCornerShape(50.dp)).testTag(tab.textId))
        }
      },
  )
}
