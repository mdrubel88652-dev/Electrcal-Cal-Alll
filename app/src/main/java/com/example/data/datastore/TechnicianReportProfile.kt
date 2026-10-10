package com.example.data.datastore

data class TechnicianReportProfile(
    val name: String = "",
    val company: String = "",
    val contactPhone: String = "",
    val email: String = "",
    val officialAddress: String = ""
) {
    fun isNotEmpty(): Boolean =
        name.isNotBlank() || company.isNotBlank() || contactPhone.isNotBlank() || email.isNotBlank() || officialAddress.isNotBlank()

    fun isEmpty(): Boolean = !isNotEmpty()
}

data class EngineerReportProfile(
    val name: String = "Rubel Electrical Engineer & Developer",
    val company: String = "Industrial Electrical & Power Engineering",
    val contactPhone: String = "+880 16094-92193",
    val email: String = "mdrubel88652@gmail.com",
    val officialAddress: String = "Power Systems Engineering Division"
) {
    companion object {
        val DEFAULT = EngineerReportProfile()
    }
}
