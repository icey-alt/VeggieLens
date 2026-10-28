package com.example.veggielens.ui.designsystem

import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver

/** 让多选分类集合可以放进 rememberSaveable（转成 List 存入 Bundle）。 */
val CategorySelectionSaver: Saver<Set<String>, Any> = listSaver(
    save = { it.toList() },
    restore = { it.toSet() },
)
