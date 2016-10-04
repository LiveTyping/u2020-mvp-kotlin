package ru.ltst.u2020mvp.data.api.model.response

import ru.ltst.u2020mvp.util.Strings

//TODO(eg): remove builder
class Image {
    val id: String

    val link: String
    val title: String
    val description: String

    val width: Int
    val height: Int
    val datetime: Long
    val views: Int
    val is_album: Boolean

    constructor(id: String, link: String, title: String, description: String,
                width: Int, height: Int, datetime: Long, views: Int, is_album: Boolean) {
        this.id = id
        this.link = link
        this.title = title
        this.description = description
        this.width = width
        this.height = height
        this.datetime = datetime
        this.views = views
        this.is_album = is_album
    }

    constructor(builder: Builder) {
        this.id = builder.id
        this.link = builder.link
        this.title = builder.title
        this.description = builder.description
        this.width = builder.width
        this.height = builder.height
        this.datetime = builder.datetime
        this.views = builder.views
        this.is_album = builder.is_album
    }

    class Builder {
        internal var id: String = Strings.EMPTY

        internal var link: String = Strings.EMPTY
        internal var title: String = Strings.EMPTY
        internal var description: String = Strings.EMPTY

        internal var width: Int = 0
        internal var height: Int = 0
        internal var datetime: Long = 0
        internal var views: Int = 0
        internal var is_album: Boolean = false

        fun setId(id: String): Builder {
            this.id = id
            return this
        }

        fun setLink(link: String): Builder {
            this.link = link
            return this
        }

        fun setTitle(title: String): Builder {
            this.title = title
            return this
        }

        fun setDescription(description: String): Builder {
            this.description = description
            return this
        }

        fun setWidth(width: Int): Builder {
            this.width = width
            return this
        }

        fun setHeight(height: Int): Builder {
            this.height = height
            return this
        }

        fun setDatetime(datetime: Long): Builder {
            this.datetime = datetime
            return this
        }

        fun setViews(views: Int): Builder {
            this.views = views
            return this
        }

        fun setIsAlbum(is_album: Boolean): Builder {
            this.is_album = is_album
            return this
        }

        fun build(): Image {
            return Image(this)
        }
    }
}
