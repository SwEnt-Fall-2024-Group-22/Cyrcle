package com.github.se.cyrcle.ui.theme.molecules

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color.Companion.LightGray
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.se.cyrcle.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.se.cyrcle.ui.navigation.NavigationActions
import com.github.se.cyrcle.ui.navigation.TopLevelDestination
import com.github.se.cyrcle.ui.theme.Black

/**
 * Create a themed bottom navigation bar, with simplified arguments. The name isn't correct anymore,
 * because it was updated to Material 3 (which isn't the same).
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
    testTag: String = "NavigationBar"
) {
  NavigationBar(
      modifier = Modifier.testTag(testTag).fillMaxWidth().height(60.dp),
      containerColor = MaterialTheme.colorScheme.surfaceContainer,
      content = {
        tabList.forEach { tab ->
          NavigationBarItem(
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
              modifier = Modifier.clip(RoundedCornerShape(50.dp)).testTag(tab.textId),
              colors =
                  NavigationBarItemColors(
                      disabledTextColor = LightGray,
                      disabledIconColor = LightGray,
                      unselectedTextColor = Black,
                      unselectedIconColor = Black,
                      selectedIconColor = MaterialTheme.colorScheme.onSurface,
                      selectedTextColor = MaterialTheme.colorScheme.onSurface,
                      selectedIndicatorColor = MaterialTheme.colorScheme.surfaceTint))
        }
      },
  )
}
