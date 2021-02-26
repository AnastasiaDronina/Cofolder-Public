package com.dronina.cofolder.data.model.other

class Issue(val id: String = "", var user: String, var text: String = "") {
    constructor() : this("", "", "")
}