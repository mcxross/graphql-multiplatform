package xyz.mcxross.graphql.plugin.gradle.client.generator.extensions


/**
 * This is the recommended approach now with the deprecation of String.capitalize from the Kotlin
 * stdlib in version 1.5.
 */
fun String.capitalizeFirstChar(): String = replaceFirstChar {
  if (it.isLowerCase()) it.uppercaseChar() else it
}

fun String.toUpperUnderscore(): String {
  val builder = StringBuilder()
  val nameCharArray = this.toCharArray()
  for ((index, c) in nameCharArray.withIndex()) {
    if (c.isUpperCase() && index > 0) {
      if (
        nameCharArray[index - 1].isLowerCase() ||
        (index < nameCharArray.size - 1 && nameCharArray[index + 1].isLowerCase())
      ) {
        builder.append("_")
      }
    }
    builder.append(c.uppercaseChar())
  }
  return builder.toString()
}
