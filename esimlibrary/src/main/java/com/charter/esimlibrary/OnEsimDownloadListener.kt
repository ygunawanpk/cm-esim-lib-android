package com.charter.esimlibrary

interface OnEsimDownloadListener {
    fun onSuccess(result: String)

    fun onFailure(result: String)
}