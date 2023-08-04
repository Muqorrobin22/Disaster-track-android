package com.example.disastertrack.model.service

import com.example.disastertrack.model.data.ReportsData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ReportApiService {
    @GET("reports/archive")
    fun getReportByYearStartToEnd(
        @Query("start") start: String,
        @Query("end") end: String
    ) : Call<ReportsData>

    @GET("reports")
    fun getReportByTimeperiod(
        @Query("timeperiod") timePeriod: Int,
    ) : Call<ReportsData>

    @GET("reports")
    fun getReportByDisaster(
        @Query("disaster") disaster: String
    ) : Call<ReportsData>

    @GET("reports")
    fun getReportByProvinceLocation(
        @Query("admin") province: String
    ) : Call<ReportsData>
}