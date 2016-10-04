package ru.ltst.u2020mvp.data.api.model

import org.threeten.bp.Instant

import ru.ltst.u2020mvp.util.Preconditions

import ru.ltst.u2020mvp.util.Preconditions.checkNotNull

//TODO(eg): move it to data class
class Repository private constructor(builder: Repository.Builder) {
    val name: String
    val owner: User
    val description: String?

    val watchers: Long
    val forks: Long

    val html_url: String

    val updated_at: Instant

    init {
        this.name = checkNotNull<String>(builder.name, "name == null")
        this.owner = checkNotNull<User>(builder.owner, "owner == null")
        this.description = builder.description
        this.watchers = builder.stars
        this.forks = builder.forks
        this.html_url = checkNotNull<String>(builder.htmlUrl, "html_url == null")
        this.updated_at = checkNotNull<Instant>(builder.updatedAt, "updated_at == null")
    }

    override fun toString(): String {
        return "Repository{" +
                "name='" + name + '\'' +
                ", owner=" + owner +
                ", description='" + description + '\'' +
                ", watchers=" + watchers +
                ", forks=" + forks +
                ", html_url='" + html_url + '\'' +
                ", updated_at=" + updated_at +
                '}'
    }

    class Builder {
        internal var name: String? = null
        internal var owner: User? = null
        internal var description: String? = null
        internal var stars: Long = 0
        internal var forks: Long = 0
        internal var htmlUrl: String? = null
        internal var updatedAt: Instant? = null

        fun name(name: String): Builder {
            this.name = name
            return this
        }

        fun owner(owner: User): Builder {
            this.owner = owner
            return this
        }

        fun description(description: String): Builder {
            this.description = description
            return this
        }

        fun stars(stars: Long): Builder {
            this.stars = stars
            return this
        }

        fun forks(forks: Long): Builder {
            this.forks = forks
            return this
        }

        fun htmlUrl(htmlUrl: String): Builder {
            this.htmlUrl = htmlUrl
            return this
        }

        fun updatedAt(updatedAt: Instant): Builder {
            this.updatedAt = updatedAt
            return this
        }

        fun build(): Repository {
            return Repository(this)
        }
    }
}
