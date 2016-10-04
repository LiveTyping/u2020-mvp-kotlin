package ru.ltst.u2020mvp


import ru.ltst.u2020mvp.data.LumberYard

interface InternalU2020Graph : U2020Graph {
    fun lumberYard(): LumberYard
    fun inject(debugApp: InternalU2020App)
}
