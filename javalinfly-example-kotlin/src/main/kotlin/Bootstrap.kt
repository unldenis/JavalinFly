package com.github.unldenis
import com.github.unldenis.javalinfly.JavalinFlyInjector

import io.javalin.security.RouteRole



enum class MyRoles : RouteRole {
    GUEST,
    USER,
    ADMIN
}