package com.water.soak

/**
 * Date：2024/8/13
 * Describe:
 */
interface ConfigureChange {
    fun changeBean(status: String, period: Long, fileName: String)

    fun loadAdSuccess()

    fun actionStatus(isSuccess: Boolean)
}