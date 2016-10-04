package ru.ltst.u2020mvp


class TestU2020Application : U2020App() {
    lateinit override var component: U2020Component

    override fun buildComponentAndInject() {
        component = DaggerTestU2020Component.builder()
                .u2020AppModule(U2020AppModule(this))
                .build()
        component.inject(this)
    }

    override fun component(): U2020Component {
        return component
    }
}
