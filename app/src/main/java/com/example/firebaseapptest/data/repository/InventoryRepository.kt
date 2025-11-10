package com.example.firebaseapptest.data.repository

import com.example.firebaseapptest.data.local.dao.ItemDao
import com.example.firebaseapptest.data.local.dao.SaleDao
import com.example.firebaseapptest.data.local.dao.SaleItemDao
import com.example.firebaseapptest.data.local.dao.UserDao
import com.example.firebaseapptest.data.local.entity.Item
import com.example.firebaseapptest.data.local.entity.Sale
import com.example.firebaseapptest.data.local.entity.SaleItem
import com.example.firebaseapptest.data.local.entity.User
import com.example.firebaseapptest.data.local.entity.helpermodels.SaleWithItemNames
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import java.security.MessageDigest
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

class InventoryRepository @Inject constructor(
    private val itemDao: ItemDao,
    private val saleDao: SaleDao,
    private val saleItemDao: SaleItemDao,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseFirestore: FirebaseFirestore,
    private val userDao: UserDao
) {
    suspend fun getItemByCodeForSale(code: Long) = itemDao.getItemByCodeForSale(code)
    suspend fun getItem(code: Long) = itemDao.getItemByCode(code)
    suspend fun getItemsCount() = itemDao.getCount()

    fun getAllItemsPaginated(limit: Int, offset: Int) = itemDao.getItemsPaginated(limit, offset)

    fun getSearchQueryPaginated(query: String, limit: Int, offset: Int) =
        itemDao.getSearchQueryPaginated(query, limit, offset)

    suspend fun upsertItem(item: Item): Long {
        return try {
            withTimeout(3000) {
                firebaseFirestore.collection("items")
                    .document(item.code.toString())
                    .set(item)
                    .await() // will throw if it fails or times out
            }
            itemDao.upsertItem(item.copy(needSync = false))
        } catch (e: Exception) {
            // includes TimeoutCancellationException
            itemDao.upsertItem(item.copy(needSync = true))
        }
    }


//    suspend fun upsertItem(item: Item) = itemDao.upsertItem(item)

    suspend fun itemSold(code: Long, amount: Int) = itemDao.addSold(code, amount)
    suspend fun deleteItem(item: Item?): Int {
        if (item != null) {
            try {
                withTimeout(3000) {
                    firebaseFirestore.collection("items")
                        .document(item.code.toString())
                        .delete()
                        .await()
                }
                return itemDao.deleteItem(item)
            } catch (e: Exception) {
                itemDao.upsertItem(item.copy(needSync = true, needToDelete = true))
                return 0
            }
        } else {
            return 0
        }
    }

    suspend fun getSaleCount() = saleDao.getCount()
    suspend fun getSaleCountBetween(start: Long, end: Long) = saleDao.getCountBetween(start, end)
    suspend fun getSaleCountToday(): Int {
        val now = LocalDate.now()
        val startOfDay = now.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli() - 1
        return saleDao.getCountBetween(startOfDay, endOfDay)
    }

    fun getSalesPaginated(limit: Int, offset: Int) = saleDao.getSalesPaginated(limit, offset)
    fun getSalesBetween(startOfDay: Long, endOfDay: Long, limit: Int, offset: Int) =
        saleDao.getSalesBetween(startOfDay, endOfDay, limit, offset)

    fun getAllSalesBetween(startOfDay: Long, endOfDay: Long) =
        saleDao.getSalesWithItemNamesBetween(startOfDay, endOfDay)

    fun getTodayAllSales(): Flow<List<SaleWithItemNames>> {
        val now = LocalDate.now()
        val startOfDay = now.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli() - 1

        return saleDao.getSalesWithItemNamesBetween(startOfDay, endOfDay)
    }

    fun getTodaySalesPaginated(limit: Int, offset: Int): Flow<List<Sale>> {
        val now = LocalDate.now()
        val startOfDay = now.atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli()
        val endOfDay = now.plusDays(1).atStartOfDay(ZoneId.systemDefault())
            .toInstant().toEpochMilli() - 1

        return saleDao.getSalesBetween(startOfDay, endOfDay, limit, offset)
    }

    suspend fun getSaleWithItems(saleId: Int) = saleDao.getSaleWithItemNames(saleId)

    //    suspend fun addSale(sale: Sale) = saleDao.addSale(sale)
    suspend fun addSale(sale: Sale): Long {
        return try {
            withTimeout(3000) {
                firebaseFirestore.collection("sales")
                    .document(sale.id.toString())
                    .set(sale)
                    .await()
            }
            saleDao.addSale(sale.copy(needSync = false))
        } catch (e: Exception) {
            saleDao.addSale(sale.copy(needSync = true))
        }
    }

    suspend fun deleteSale(sale: Sale) = saleDao.deleteSale(sale)

    //    suspend fun addSaleItems(saleItems: List<SaleItem>) = saleItemDao.insertSaleItems(saleItems)
    suspend fun addSaleItems(saleItems: List<SaleItem>): List<Long> {
        return try {
            withTimeout(5000) {
                val batch = firebaseFirestore.batch()

                saleItems.forEach { saleItem ->
                    val itemRef = firebaseFirestore.collection("items")
                        .document(saleItem.id.toString())
                    batch.set(itemRef, saleItem) // write each item
                }

                batch.commit().await() // wait for all writes to finish
            }
            // Save locally as synced
            saleItemDao.upsertSaleItems(saleItems.map { it.copy(needSync = false) })
        } catch (e: Exception) {
            // If Firestore fails, mark for sync later
            saleItemDao.upsertSaleItems(saleItems.map { it.copy(needSync = true) })
        }
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256")
            .digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }


    suspend fun login(email: String, password: String): LoginResult {
        return try {
            // Firebase sign-in
            val authResult = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            // Fetch user document from Firestore
            val snapshot = firebaseFirestore.collection("users")
                .whereEqualTo("email", email)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val snap = snapshot.documents[0]
//                    .toObject(User::class.java)
                val user = User(
                    email = snap.getString("email") ?: "",
                    username = snap.getString("username") ?: "",
                )
                if (snap != null) {
                    LoginResult.Success(user)
                } else {
                    LoginResult.UnknownError(Exception("User document parse failed"))
                }
            } else {
                LoginResult.UserNotFound
            }
        } catch (e: Exception) {
            when (e) {
                is FirebaseAuthInvalidUserException -> LoginResult.UserNotFound
                is FirebaseAuthInvalidCredentialsException -> LoginResult.WrongPassword
                else -> {

                    // Fallback to Room DB
                    val user = userDao.getUserByEmail(email)
                    val hashedPassword = hashPassword(password)

                    if (user == null) {
                        LoginResult.UserNotFound
                    } else if (user.password != hashedPassword) {
                        LoginResult.WrongPassword
                    } else {
                        LoginResult.Success(user)
                    }
                }
            }
        }
    }

    suspend fun OutgoingSyncItems(): Boolean {
        try {
            val batch = firebaseFirestore.batch()

            itemDao.getItemsToSync().forEach { item ->

                if (item.needToDelete) {
                    val itemRef = firebaseFirestore.collection("items")
                        .document(item.code.toString())

                    batch.delete(itemRef)

                    itemDao.deleteItem(item)
                } else {
                    val itemRef = firebaseFirestore.collection("items")
                        .document(item.code.toString())

                    batch.set(itemRef, item.copy(needSync = false))

                    itemDao.upsertItem(item.copy(needSync = false))
                }
            }

            saleDao.getSalesToSync().forEach { sale ->
                val saleRef = firebaseFirestore.collection("sales")
                    .document(sale.id.toString())

                batch.set(saleRef, sale.copy(needSync = false))

                saleDao.addSale(sale.copy(needSync = false))
            }

            saleItemDao.getSaleItemsToSync().forEach { saleItem ->
                val saleItemRef = firebaseFirestore.collection("saleItems")
                    .document(saleItem.id.toString())

                batch.set(saleItemRef, saleItem.copy(needSync = false))

                saleItemDao.upsertSaleItems(listOf(saleItem.copy(needSync = false)))
            }

            batch.commit()
                .await()

            return true
        } catch (e: Exception) {
            return false
        }
    }
}
