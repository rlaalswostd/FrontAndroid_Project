package com.example.neworderapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.neworderapp.client.TableCheckerClient
import com.example.neworderapp.dto.TableStatusResponse
import com.example.neworderapp.service.TableChecker
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SetTableActivity : AppCompatActivity() {

    private lateinit var tableNumberEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var tablePreferenceManager: TablePreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_set_table)

        tableNumberEditText = findViewById(R.id.tableNumberEditText)
        saveButton = findViewById(R.id.saveButton)
        tablePreferenceManager = TablePreferenceManager(this)

        // 저장 버튼 클릭 시 테이블 번호 확인 후 저장
        saveButton.setOnClickListener {
            val tableNumber = tableNumberEditText.text.toString()
            val storeId = intent.getStringExtra("newStoreId")

            if (tableNumber.isNotEmpty()) {
                if (storeId != null) {
                    checkTableExists(storeId, tableNumber)  // 입력한 tableNumber 사용
                } else {
                    Toast.makeText(this, "Store ID가 전달되지 않았습니다.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "테이블 번호를 입력하세요.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun checkTableExists(storeId: String, tableNumber: String) {
        val tableChecker = TableCheckerClient.tableCheckerInstance.create(TableChecker::class.java)

        // 테이블 존재 여부와 상태를 확인
        tableChecker.checkTableExistsandoccu(storeId, tableNumber).enqueue(object : Callback<TableStatusResponse> {
            override fun onResponse(call: Call<TableStatusResponse>, response: Response<TableStatusResponse>) {
                if (response.isSuccessful) {
                    val tableStatus = response.body()

                    if (tableStatus != null) {
                        if (tableStatus.tableExists) {
                            // 테이블이 존재할 때, occupied 상태를 확인
                            if (tableStatus.occupied) {
                                // occupied == true: 사용 가능
                                saveTableNumberAndFinish(tableNumber)
                            } else {
                                // occupied == false: 사용 불가
                                Toast.makeText(this@SetTableActivity, "해당 테이블은 사용 불가 상태입니다.", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // 테이블이 존재하지 않는 경우
                            Toast.makeText(this@SetTableActivity, "해당 테이블은 존재하지 않습니다.", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@SetTableActivity, "응답이 유효하지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this@SetTableActivity, "서버 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<TableStatusResponse>, t: Throwable) {
                Toast.makeText(this@SetTableActivity, "네트워크 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
                t.printStackTrace()  // 에러 메시지 출력
            }
        })
    }

    private fun saveTableNumberAndFinish(tableNumber: String) {
        tablePreferenceManager.saveTableNumber(tableNumber)  // tableNumber를 저장
        val resultIntent = Intent().apply {
            putExtra("tableNumber", tableNumber)  // tableNumber를 결과로 전달
        }
        setResult(RESULT_OK, resultIntent)
        finish()
        Toast.makeText(this, "성공적으로 저장되었습니다. 앱을 재실행해주세요.", Toast.LENGTH_LONG).show()
    }
}