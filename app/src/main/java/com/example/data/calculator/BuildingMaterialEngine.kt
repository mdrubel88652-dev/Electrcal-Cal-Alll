package com.example.data.calculator

import com.example.data.model.BuildingProjectData
import com.example.data.model.MaterialItem
import kotlin.math.ceil

object BuildingMaterialEngine {

    fun calculateMaterials(project: BuildingProjectData): List<MaterialItem> {
        val list = mutableListOf<MaterialItem>()
        var itemNo = 1

        val floors = project.floors.coerceAtLeast(1)
        val rooms = project.rooms.coerceAtLeast(1)
        val points = project.points

        val lights = points["light"] ?: 0
        val fans = points["fan"] ?: 0
        val sockets = points["socket"] ?: 0
        val acs = points["ac"] ?: 0
        val geysers = points["geyser"] ?: 0
        val pumps = points["pump"] ?: 0
        val tvs = points["tv"] ?: 0
        val datas = points["data"] ?: 0
        val bells = points["bell"] ?: 0
        val exhaustFans = points["exhaust"] ?: 0
        val others = points["other"] ?: 0

        val totalPoints = lights + fans + sockets + acs + geysers + pumps + tvs + datas + bells + exhaustFans + others

        // 1. Wires & Cables Calculation (Standard coils of 100 meters / 90 yards)
        // 1.5 mm² for Lighting, Fans, Bells, Exhaust: approx 14 meters per point (phase + neutral)
        val lightLoadPoints = lights + fans + bells + exhaustFans
        val wire1_5Meters = (lightLoadPoints * 14.0 * 1.15).coerceAtLeast(50.0)
        val coils1_5 = ceil(wire1_5Meters / 100.0).toInt().coerceAtLeast(1)
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Single Core Copper Wire (BYA)",
                specification = "1.5 mm² (1x1.5 RM) FR Grade",
                brand = project.wireBrand,
                unit = "Coil (100m)",
                quantity = coils1_5 * 2, // Red (Phase) + Black (Neutral)
                remarks = "Lighting, Ceiling Fans & Exhaust Circuits"
            )
        )

        // 2.5 mm² for 13A Power Sockets & General Power: approx 18 meters per socket
        val wire2_5Meters = (sockets * 18.0 * 1.15).coerceAtLeast(40.0)
        val coils2_5 = ceil(wire2_5Meters / 100.0).toInt().coerceAtLeast(1)
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Single Core Copper Wire (BYA)",
                specification = "2.5 mm² (1x2.5 RM) FR Grade",
                brand = project.wireBrand,
                unit = "Coil (100m)",
                quantity = coils2_5 * 2, // Phase + Neutral
                remarks = "13A Multi-Plug Power Socket Outlets"
            )
        )

        // 4.0 mm² for Air Conditioners (AC) & Water Geysers: approx 22 meters per heavy point
        val heavyPoints = acs + geysers
        if (heavyPoints > 0) {
            val wire4Meters = heavyPoints * 22.0 * 1.15
            val coils4 = ceil(wire4Meters / 100.0).toInt().coerceAtLeast(1)
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "Single Core Copper Wire (BYA)",
                    specification = "4.0 mm² (1x4.0 RM) FR Grade",
                    brand = project.wireBrand,
                    unit = "Coil (100m)",
                    quantity = coils4 * 2,
                    remarks = "Dedicated Dedicated Lines for AC & Water Heaters"
                )
            )
        }

        // 6.0 mm² for Water Pump & Sub-DB Riser
        if (pumps > 0 || floors > 1) {
            val pumpCableMeters = (pumps * 25.0) + (floors * 18.0)
            val coils6 = ceil(pumpCableMeters / 100.0).toInt().coerceAtLeast(1)
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "Single Core Copper Wire (BYA)",
                    specification = "6.0 mm² (1x6.0 RM) FR Grade",
                    brand = project.wireBrand,
                    unit = "Coil (100m)",
                    quantity = coils6 * 2,
                    remarks = "Water Pump Motor & Floor Sub-Feeder Lines"
                )
            )
        }

        // 10.0 mm² or 16.0 mm² Main Service Feeder Cable
        val mainCableLength = (floors * 20.0 + 35.0).toInt()
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Armoured / NYY Power Cable",
                specification = if (floors > 2) "4 Core x 16 mm² Copper NYY" else "2 Core x 10 mm² Copper BYM",
                brand = project.wireBrand,
                unit = "Meter",
                quantity = mainCableLength,
                remarks = "Main Service Entrance from Meter to Main DB"
            )
        )

        // Earth Wire (Green / Yellow) 1.5mm² and 2.5mm²
        val earthCoils = ceil((coils1_5 + coils2_5) * 0.6).toInt().coerceAtLeast(1)
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Earth Continuity Conductor (ECC)",
                specification = "1.5 mm² & 2.5 mm² Green/Yellow",
                brand = project.wireBrand,
                unit = "Coil (100m)",
                quantity = earthCoils,
                remarks = "Appliance Earthing & Shock Protection Grounding"
            )
        )

        // 2. Conduits & Piping
        val conduitMeters = (totalPoints * 4.5 * 1.1).coerceAtLeast(60.0)
        val conduitLengths = ceil(conduitMeters / 3.0).toInt() // Standard 3m / 10ft pipe
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "uPVC Electrical Conduit Pipe",
                specification = "20 mm (3/4 inch) Heavy Duty Medium Gauge",
                brand = project.pipeBrand,
                unit = "Length (3m / 10ft)",
                quantity = conduitLengths,
                remarks = "Concealed Wall & Ceiling Slab Chasing"
            )
        )

        val conduit25Lengths = ceil(conduitLengths * 0.25).toInt().coerceAtLeast(5)
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "uPVC Electrical Conduit Pipe",
                specification = "25 mm (1 inch) Heavy Duty Medium Gauge",
                brand = project.pipeBrand,
                unit = "Length (3m / 10ft)",
                quantity = conduit25Lengths,
                remarks = "Sub-main Feeder & AC Cable Trunking"
            )
        )

        list.add(
            MaterialItem(
                no = itemNo++,
                name = "PVC Flexible Conduit Corrugated Pipe",
                specification = "20 mm Flexible Pipe Coil",
                brand = project.pipeBrand,
                unit = "Coil (50m)",
                quantity = (floors * 2).coerceAtLeast(2),
                remarks = "Drop Ceilings, Beams & Switch Box Connections"
            )
        )

        // 3. Boxes & Conduit Fittings
        val circularJunctionBoxes = ceil(totalPoints * 0.9).toInt().coerceAtLeast(10)
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "PVC Circular Junction Box",
                specification = "3-Way / 4-Way 20mm with Lid",
                brand = project.pipeBrand,
                unit = "pcs",
                quantity = circularJunctionBoxes,
                remarks = "Wiring Junctions & Ceiling Point Drops"
            )
        )

        val ceilingFanHooks = fans.coerceAtLeast(1)
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "GI Ceiling Fan Hook Box",
                specification = "Heavy Duty Cast Iron / GI Clamp",
                brand = "Standard Galvanized",
                unit = "pcs",
                quantity = ceilingFanHooks,
                remarks = "Ceiling Slab Fan Anchorage"
            )
        )

        // Switch Boxes
        val switchBoxes = rooms * 3 + floors * 2
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Modular Concealed Metal/PVC Switch Box",
                specification = "4-Module & 6-Module Gang Box",
                brand = project.switchBrand,
                unit = "pcs",
                quantity = switchBoxes,
                remarks = "Wall Embedded Modular Enclosures"
            )
        )

        val socketBoxes = sockets.coerceAtLeast(rooms * 2)
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Modular Power Outlet Box",
                specification = "2-Module / 3-Module Box",
                brand = project.switchBrand,
                unit = "pcs",
                quantity = socketBoxes,
                remarks = "Heavy Appliance & Power Sockets"
            )
        )

        // 4. Wiring Devices & Switches
        val switchesCount = lights + (exhaustFans * 1) + 4
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Modular 1-Way / 2-Way Light Switch",
                specification = "10A 250V Piano Key / Rocker",
                brand = project.switchBrand,
                unit = "pcs",
                quantity = switchesCount,
                remarks = "Lighting & General Load Control"
            )
        )

        if (fans > 0) {
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "Stepless Fan Speed Regulator",
                    specification = "Modular Dimmer Type (300W)",
                    brand = project.switchBrand,
                    unit = "pcs",
                    quantity = fans,
                    remarks = "Ceiling Fan Speed Adjustment"
                )
            )
        }

        if (sockets > 0) {
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "Universal Multi-Plug Power Socket with Switch",
                    specification = "13A / 16A 3-Pin with Child Shutter",
                    brand = project.switchBrand,
                    unit = "pcs",
                    quantity = sockets,
                    remarks = "Room TV, Appliances & Device Outlets"
                )
            )
        }

        if (acs > 0) {
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "AC Power Outlet Unit with DP Switch",
                    specification = "20A / 25A Double Pole with Neon Indicator",
                    brand = project.switchBrand,
                    unit = "pcs",
                    quantity = acs,
                    remarks = "Dedicated Air Conditioner Isolator"
                )
            )
        }

        if (geysers > 0) {
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "Water Heater / Geyser DP Switch",
                    specification = "20A Double Pole with Indicator",
                    brand = project.switchBrand,
                    unit = "pcs",
                    quantity = geysers,
                    remarks = "Bathroom Geyser Point"
                )
            )
        }

        if (tvs > 0) {
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "TV Coaxial Antenna Socket",
                    specification = "Modular TV Satellite Outlet",
                    brand = project.switchBrand,
                    unit = "pcs",
                    quantity = tvs,
                    remarks = "Living & Bed Room Cable TV Point"
                )
            )
        }

        if (datas > 0) {
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "RJ45 Cat6 Data / Network Socket",
                    specification = "Modular LAN Keystone Jack",
                    brand = project.switchBrand,
                    unit = "pcs",
                    quantity = datas,
                    remarks = "High-speed Internet & Wi-Fi Router Outlet"
                )
            )
        }

        if (bells > 0) {
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "Door Bell Push Switch & Chime",
                    specification = "10A Bell Indicator Switch + Ding Dong Chime",
                    brand = project.switchBrand,
                    unit = "Set",
                    quantity = bells,
                    remarks = "Main Entrance Door Chime"
                )
            )
        }

        // 5. Distribution Boards & Circuit Protection
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Main Distribution Board (MDB)",
                specification = if (floors > 1) "8-12 Way TPN Distribution Board (Metal Enclosure)" else "6-8 Way SPN DB",
                brand = "Havells / Schneider / Standard",
                unit = "pcs",
                quantity = 1,
                remarks = "Incoming Supply Protection Center"
            )
        )

        if (floors > 1) {
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "Floor Sub-Distribution Board (SDB)",
                    specification = "6-8 Way SPN Flush Mounting DB",
                    brand = "Havells / Schneider / Standard",
                    unit = "pcs",
                    quantity = floors,
                    remarks = "Independent Floor Isolation Sub-Panel"
                )
            )
        }

        // Circuit Breakers
        val lightCircuits = ceil(lightLoadPoints / 8.0).toInt().coerceAtLeast(1)
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Miniature Circuit Breaker (MCB)",
                specification = "6A / 10A Single Pole (SP) Type-B 6kA",
                brand = "ABB / Schneider / Siemens",
                unit = "pcs",
                quantity = lightCircuits * floors,
                remarks = "Branch Circuit Lighting Protection"
            )
        )

        val powerCircuits = ceil(sockets / 4.0).toInt().coerceAtLeast(1)
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Miniature Circuit Breaker (MCB)",
                specification = "16A / 20A Single Pole (SP) Type-C 6kA",
                brand = "ABB / Schneider / Siemens",
                unit = "pcs",
                quantity = powerCircuits * floors,
                remarks = "Branch Power Socket Ring/Radial Feeder"
            )
        )

        if (heavyPoints > 0) {
            list.add(
                MaterialItem(
                    no = itemNo++,
                    name = "Miniature Circuit Breaker (MCB)",
                    specification = "25A / 32A Single Pole (SP) Type-C 6kA",
                    brand = "ABB / Schneider / Siemens",
                    unit = "pcs",
                    quantity = heavyPoints,
                    remarks = "Dedicated AC & Geyser Circuit Protection"
                )
            )
        }

        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Residual Current Circuit Breaker (RCCB / ELCB)",
                specification = "40A / 63A 2-Pole / 4-Pole 30mA Sensitivity",
                brand = "ABB / Schneider / Siemens",
                unit = "pcs",
                quantity = floors,
                remarks = "Mandatory Human Life Shock & Earth Leakage Protection"
            )
        )

        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Main Isolator Switch / MCCB",
                specification = if (floors > 2) "100A 4-Pole Main Moulded Case Breaker" else "63A Double Pole Isolator",
                brand = "ABB / Schneider / Siemens",
                unit = "pcs",
                quantity = 1,
                remarks = "Master Emergency Disconnect Switch"
            )
        )

        // 6. Earthing & Grounding
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Pure Copper Earth Electrode Rod",
                specification = "5/8 inch (16mm) Diameter x 10 Feet Length",
                brand = "Heavy Copper Bonded",
                unit = "pcs",
                quantity = if (floors > 3) 2 else 1,
                remarks = "Substation / Building Main Grounding"
            )
        )

        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Bare Copper Grounding Wire",
                specification = "16 mm² / 25 mm² Solid Copper Conductor",
                brand = project.wireBrand,
                unit = "Meter",
                quantity = (floors * 12 + 15),
                remarks = "Connection from DB Earth Bar to Earth Pit"
            )
        )

        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Cast Brass Earth Clamp & Pit Chamber",
                specification = "Heavy Duty Mechanical Rod Clamp with PVC Inspection Cover",
                brand = "Standard",
                unit = "Set",
                quantity = 1,
                remarks = "Soil Earth Pit Enclosure"
            )
        )

        // 7. Installation Accessories
        list.add(
            MaterialItem(
                no = itemNo++,
                name = "PVC Conduit Bends & Couplers",
                specification = "20mm Elbow Bends, Sockets & Tee",
                brand = project.pipeBrand,
                unit = "Pack (100 pcs)",
                quantity = ceil(totalPoints * 0.4 / 100.0).toInt().coerceAtLeast(1),
                remarks = "Conduit Routing & Turns"
            )
        )

        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Galvanized GI Pipe Saddles / Clips",
                specification = "20mm & 25mm Metal Saddles",
                brand = "Standard GI",
                unit = "pcs",
                quantity = (conduitLengths * 4).coerceAtLeast(50),
                remarks = "Conduit Pipe Clamping & Anchoring"
            )
        )

        list.add(
            MaterialItem(
                no = itemNo++,
                name = "PVC Electrical Insulation Tape",
                specification = "Flame Retardant Color Coded (Red, Yellow, Blue, Black, Green)",
                brand = "3M / Standard",
                unit = "Rolls",
                quantity = (floors * 6).coerceAtLeast(6),
                remarks = "Conductor Jointing & Phase Identification"
            )
        )

        list.add(
            MaterialItem(
                no = itemNo++,
                name = "Nylon Cable Ties & Rawl Plugs with Screws",
                specification = "Assorted 150mm Ties & 35mm Wall Plugs",
                brand = "Standard",
                unit = "Packet",
                quantity = 4,
                remarks = "Panel Dressing & Wall Fixing Hardware"
            )
        )

        return list
    }
}
