package com.example.kitsutest

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kitsutest.databinding.ActivityMainBinding
import com.google.gson.annotations.SerializedName
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private val sharedPrefFile = "com.example.kitsutest"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация SharedPreferences
        sharedPreferences = getSharedPreferences(sharedPrefFile, Context.MODE_PRIVATE)

        // Проверка статуса аутентификации
        if (sharedPreferences.getBoolean("authenticated", false)) {
            // Пользователь уже аутентифицирован, перейти на другой экран
            val intent = Intent(this, MainActivity2::class.java)
            startActivity(intent)
            finish()
        } else {
            // Пользователь не аутентифицирован, показать код аутентификации
            setupAuthentication()
        }
    }

    private fun setupAuthentication() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://kitsu.io/api/")
            .client(OkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(KitsuApiService::class.java)

        with(binding) {
            click.setOnClickListener {

                val login = log.text.toString()
                val password = pass.text.toString()

                apiService.authenticate("password", login, password).enqueue(object :
                    Callback<AuthenticationDto> {

                    override fun onResponse(
                        call: Call<AuthenticationDto>,
                        response: Response<AuthenticationDto>
                    ) {
                        if (response.isSuccessful) {
                            Toast.makeText(this@MainActivity, "Вы успешно вошли", Toast.LENGTH_SHORT).show()
                            // Сохранение статуса аутентификации в SharedPreferences
                            with(sharedPreferences.edit()) {
                                putBoolean("authenticated", true)
                                apply()
                            }
                            // Переход на другой экран
                            val intent = Intent(this@MainActivity, MainActivity2::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@MainActivity, "Неверное имя пользователя или пароль", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<AuthenticationDto>, t: Throwable) {
                        Log.e("ololo", "onFailure:${t.message}")
                        Toast.makeText(this@MainActivity, "ты далбаеп", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
    interface KitsuApiService {
        @POST("oauth/token")
        @FormUrlEncoded
        fun authenticate(
            @Field("grant_type") grantType: String,
            @Field("username") username: String,
            @Field("password") password: String
        ): Call<AuthenticationDto>
    }

    data class AuthenticationDto(
        @SerializedName("access_token") val accessToken: String,
        @SerializedName("token_type") val tokenType: String,
        @SerializedName("expires_in") val expiresIn: Long,
        @SerializedName("refresh_token") val refreshToken: String,
        @SerializedName("scope") val scope: String,
        @SerializedName("created_at") val createdAt: Long
    )

}