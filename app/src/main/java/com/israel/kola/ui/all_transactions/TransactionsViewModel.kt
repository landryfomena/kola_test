package com.israel.kola.ui.all_transactions

import android.net.Uri
import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.israel.kola.data.local.Transaction
import com.israel.kola.data.local.TransactionDataSource
import com.israel.kola.models.TransactionState
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.regex.Pattern
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(private var transactionDataSource: TransactionDataSource): ViewModel() {
    val transactions = MutableLiveData<List<Transaction>>()
    val loading = MutableLiveData<Boolean>()


    public fun fetchTransactions(activity: FragmentActivity){
        val messageUri = Uri.parse("content://sms/")
        val cr = activity.contentResolver
        val cursor = cr.query(messageUri, null, null, null, null)
        activity.startManagingCursor(cursor)

        val allTransactions = arrayListOf<Transaction>()
        val smsCount = cursor?.count
        Log.e("COUNT", smsCount.toString())
        if (cursor == null)return
        var i = 0
        var j = 0

        if(cursor.moveToFirst()){
            try{
                while (i < smsCount!!){
                    val sender = cursor.getString(cursor.getColumnIndex("address"))
                    val body = cursor.getString(cursor.getColumnIndex("body")).replace(" ", "")
                    if(sender.equals("OrangeMoney") && (body.contains("IDtransaction") || body.contains("Nodetransaction"))){
                        j++
                        val matcherCredit = Pattern.compile("RC\\d+.\\d+.\\w+").matcher(body)
                        val matcherTransfer = Pattern.compile("PP\\d+.\\d+.\\w+").matcher(body)
                        val matcherWithdraw = Pattern.compile("CO\\d+.\\d+.\\w+").matcher(body)
                        val matcherDeposit = Pattern.compile("CI\\d+.\\d+.\\w+").matcher(body)

                        when{
                            matcherCredit.find() -> {
                                val transaction = msgToTransaction("Montantdelatransaction:\\d+FCFA", body, matcherCredit.group(), TransactionState.CREDIT)
                                transaction?.let {
                                    allTransactions.add(it)
                                }
                            }
                            matcherTransfer.find() -> {
                                val transaction = msgToTransaction("MontantTransaction:\\d+FCFA", body, matcherTransfer.group(), TransactionState.TRANSFER)
                                transaction?.let {
                                    allTransactions.add(it)
                                }
                            }
                            matcherWithdraw.find() -> {
                                val transaction = msgToTransaction("Montant:\\d+FCFA", body, matcherWithdraw.group(), TransactionState.WITHDRAW)
                                transaction?.let {
                                    allTransactions.add(it)
                                }
                            }
                            matcherDeposit.find() -> {
                                val transaction = msgToTransaction("Montantdetransaction:\\d+FCFA", body, matcherDeposit.group(), TransactionState.DEPOSIT)
                                transaction?.let {
                                    allTransactions.add(it)
                                }
                            }
                        }
                    }
                    i++
                    cursor.moveToNext()
                }
            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        for (t in allTransactions){
            addTransaction(t)
        }
    }

    public fun msgToTransaction(regex: String, body: String, transactionId: String, state: TransactionState): Transaction?{
        val p = Pattern.compile(regex)
        val m = p.matcher(body)
        if (m.find()){
            val amountFCFA = m.group().split(":")[1].replace("FCFA", "")
            val amount = amountFCFA.toInt()
            return Transaction(
                transactionId,
                state.toString(),
                amount
            )
        }else{
            Log.e("STATE_ERROR", state.toString())
        }
        return null
    }

    public fun addTransaction(transaction: Transaction){
        transactionDataSource.addTransaction(transaction)
    }

}