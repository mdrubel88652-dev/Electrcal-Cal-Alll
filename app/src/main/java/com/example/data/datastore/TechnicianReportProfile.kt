package com.example.data.datastore

data class TechnicianReportProfile(
    val name: String = "",
    val designation: String = "",
    val contactPhone: String = "",
    val email: String = "",
    val officialAddress: String = "",
    val photoUri: String = "",
    val licenseName: String = "",
    val licenseNumber: String = ""
) {
    fun isNotEmpty(): Boolean =
        name.isNotBlank() || designation.isNotBlank() || contactPhone.isNotBlank() ||
        email.isNotBlank() || officialAddress.isNotBlank() || photoUri.isNotBlank() ||
        licenseName.isNotBlank() || licenseNumber.isNotBlank()

    fun isEmpty(): Boolean = !isNotEmpty()
}

data class EngineerReportProfile(
    val name: String = "Rubel Electrical Engineer & Developer",
    val designation: String = "Lead Electrical Engineer & Developer",
    val email: String = "mdrubel88652@gmail.com",
    val officialAddress: String = "Power Systems Engineering Division"
) {
    companion object {
        val DEFAULT = EngineerReportProfile()
    }
}
