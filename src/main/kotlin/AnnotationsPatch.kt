package com.nickcoblentz.montoya

import burp.api.montoya.core.Annotations

public fun Annotations.appendNotes(newNotes : String) {
    if(notes().isNullOrEmpty())
        setNotes(newNotes)
    else
        setNotes("${notes()}, $newNotes")
}