package com.cybersync.nodd.data

import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class VCardTest {

    private val aarav = Profile(
        firstName = "Aarav",
        lastName = "Mehta",
        phone = "+91 98765 43210",
        email = "aarav@example.com",
        jobTitle = "Product designer",
        company = "Studio, Pune",
        instagram = "@aarav.makes",
        linkedin = "aaravmehta",
        website = "aarav.design",
    )

    @Test
    fun includesEverySwitchedOnField() {
        val card = VCard.build(aarav)
        assertTrue(card.startsWith("BEGIN:VCARD\r\nVERSION:3.0\r\n"))
        assertTrue("N:Mehta;Aarav;;;" in card)
        assertTrue("FN:Aarav Mehta" in card)
        assertTrue("TEL;TYPE=CELL:+91 98765 43210" in card)
        assertTrue("EMAIL:aarav@example.com" in card)
        assertTrue("ORG:Studio\\, Pune" in card)
        assertTrue("TITLE:Product designer" in card)
        assertTrue("URL:https://instagram.com/aarav.makes" in card)
        assertTrue("URL:https://www.linkedin.com/in/aaravmehta" in card)
        assertTrue("URL:https://aarav.design" in card)
        assertTrue(card.endsWith("END:VCARD"))
    }

    @Test
    fun leavesOutSwitchedOffFields() {
        val card = VCard.build(aarav.copy(hidden = setOf(Field.Phone, Field.Work)))
        assertFalse("TEL" in card)
        assertFalse("ORG" in card)
        assertFalse("TITLE" in card)
        assertTrue("EMAIL:aarav@example.com" in card)
    }

    @Test
    fun parsesItsOwnOutput() {
        val parsed = VCard.parse(VCard.build(aarav))
        assertNotNull(parsed)
        parsed!!
        assertEquals("Aarav Mehta", parsed.name)
        assertEquals(listOf("+91 98765 43210"), parsed.phones)
        assertEquals(listOf("aarav@example.com"), parsed.emails)
        assertEquals("Studio, Pune", parsed.company)
        assertEquals("Product designer", parsed.title)
        assertEquals(3, parsed.urls.size)
    }

    @Test
    fun parsesMeCard() {
        val parsed = VCard.parse("MECARD:N:Sharma,Riya;TEL:+919812345678;EMAIL:riya@example.com;;")
        assertEquals("Riya Sharma", parsed?.name)
        assertEquals(listOf("+919812345678"), parsed?.phones)
    }

    @Test
    fun ignoresNonContactText() {
        assertNull(VCard.parse("https://example.com"))
    }

    @Test
    fun instagramAcceptsLinksAndHandles() {
        assertEquals("riya.runs", Profile(instagram = "https://www.instagram.com/riya.runs/?hl=en").instagramHandle())
        assertEquals("riya.runs", Profile(instagram = "@riya.runs").instagramHandle())
        assertNull(Profile(instagram = "  ").instagramHandle())
    }

    @Test
    fun qrRoundTripsAsciiCard() {
        val card = VCard.build(aarav)
        assertEquals(card, decode(card))
    }

    @Test
    fun qrRoundTripsNonLatinNames() {
        val card = VCard.build(Profile(firstName = "आरव", lastName = "मेहता", phone = "+91 98765 43210"))
        assertEquals(card, decode(card))
    }

    private fun decode(text: String): String {
        val matrix = QrMatrix.encode(text)
        val scale = 4
        val width = matrix.width * scale
        val height = matrix.height * scale
        val pixels = IntArray(width * height) { i ->
            if (matrix[(i % width) / scale, (i / width) / scale]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt()
        }
        val bitmap = BinaryBitmap(HybridBinarizer(RGBLuminanceSource(width, height, pixels)))
        return QRCodeReader().decode(bitmap, mapOf(DecodeHintType.PURE_BARCODE to true)).text
    }
}
