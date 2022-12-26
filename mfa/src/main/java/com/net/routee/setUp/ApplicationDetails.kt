package com.net.routee.setUp

/**
 * The data class with following fields.
 *
 * @property applicationUUID A string value.
 * @property deviceUUID A string value.
 * @property userId A string value.
 * @property token A string value.
 */
data class ApplicationDetails(
    var applicationUUID: String? = null,
    var deviceUUID: String? = null,
    var userId: String? = null,
    var token: String? = null,
    var actionToken: String? = null,
    var actionChoice: String? = null,
    var actionValue: String? = null
)
