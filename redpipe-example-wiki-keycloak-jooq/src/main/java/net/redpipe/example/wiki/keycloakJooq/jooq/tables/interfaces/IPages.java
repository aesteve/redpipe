/*
 * This file is generated by jOOQ.
*/
package net.redpipe.example.wiki.keycloakJooq.jooq.tables.interfaces;


import io.github.jklingsporn.vertx.jooq.shared.internal.VertxPojo;

import java.io.Serializable;

import javax.annotation.Generated;


/**
 * This class is generated by jOOQ.
 */
@Generated(
    value = {
        "http://www.jooq.org",
        "jOOQ version:3.10.5"
    },
    comments = "This class is generated by jOOQ"
)
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public interface IPages extends VertxPojo, Serializable {

    /**
     * Setter for <code>public.pages.id</code>.
     */
    public IPages setId(Integer value);

    /**
     * Getter for <code>public.pages.id</code>.
     */
    public Integer getId();

    /**
     * Setter for <code>public.pages.name</code>.
     */
    public IPages setName(String value);

    /**
     * Getter for <code>public.pages.name</code>.
     */
    public String getName();

    /**
     * Setter for <code>public.pages.content</code>.
     */
    public IPages setContent(String value);

    /**
     * Getter for <code>public.pages.content</code>.
     */
    public String getContent();

    // -------------------------------------------------------------------------
    // FROM and INTO
    // -------------------------------------------------------------------------

    /**
     * Load data from another generated Record/POJO implementing the common interface IPages
     */
    public void from(net.redpipe.example.wiki.keycloakJooq.jooq.tables.interfaces.IPages from);

    /**
     * Copy data into another generated Record/POJO implementing the common interface IPages
     */
    public <E extends net.redpipe.example.wiki.keycloakJooq.jooq.tables.interfaces.IPages> E into(E into);

    @Override
    public default IPages fromJson(io.vertx.core.json.JsonObject json) {
        setId(json.getInteger("id"));
        setName(json.getString("name"));
        setContent(json.getString("content"));
        return this;
    }


    @Override
    public default io.vertx.core.json.JsonObject toJson() {
        io.vertx.core.json.JsonObject json = new io.vertx.core.json.JsonObject();
        json.put("id",getId());
        json.put("name",getName());
        json.put("content",getContent());
        return json;
    }

}
