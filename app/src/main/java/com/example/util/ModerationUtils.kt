package com.example.util

object ModerationUtils {
  fun validateName(name: String): Pair<Boolean, String?> {
    val trimmed = name.trim()
    if (trimmed.length < 2) {
      return Pair(false, "Name must be at least 2 characters")
    }
    if (trimmed.length > 24) {
      return Pair(false, "Name must be 24 characters or fewer")
    }
    val regex = Regex("^[a-zA-Z\\s-]+$")
    if (!regex.matches(trimmed)) {
      return Pair(false, "Please use only letters, hyphens, and spaces")
    }
    return Pair(true, null)
  }

  fun validateHomeworkTitle(title: String): Pair<Boolean, String?> {
    val trimmed = title.trim()
    if (trimmed.length < 2) {
      return Pair(false, "Title must be at least 2 characters")
    }
    if (trimmed.length > 80) {
      return Pair(false, "Title must be 80 characters or fewer")
    }
    return Pair(true, null)
  }

  fun validateDescription(desc: String): Pair<Boolean, String?> {
    if (desc.length > 600) {
      return Pair(false, "Description must be 600 characters or fewer")
    }
    return Pair(true, null)
  }

  fun sanitizeText(text: String): String {
    return text.trim().replace(Regex("\\s+"), " ")
  }

  fun containsBlockedWords(text: String, blockedWordsCsv: String): Pair<Boolean, String?> {
    val blockedList = blockedWordsCsv
      .split(",")
      .map { it.trim().lowercase() }
      .filter { it.isNotEmpty() }

    val lower = text.lowercase()
    for (word in blockedList) {
      val wordPattern = Regex("\\b${Regex.escape(word)}\\b")
      if (wordPattern.containsMatchIn(lower)) {
        return Pair(true, "Please revise: inappropriate word \"$word\" detected")
      }
    }
    return Pair(false, null)
  }
}
