///*
// * Copyright 2016 Google Inc. All Rights Reserved.
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// * http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package com.example.capstone2021;
//
//import android.app.Activity
//import android.content.pm.PackageManager
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import java.util.*
//
//
//object PermissionUtils {
//    fun requestPermission(
//        activity: Activity?, requestCode: Int, vararg permissions: String
//    ): Boolean {
//        var granted = true
//        val permissionsNeeded = ArrayList<String>()
//        for (s in permissions) {
//            val permissionCheck: Int = ContextCompat.checkSelfPermission(activity, s)
//            val hasPermission = permissionCheck == PackageManager.PERMISSION_GRANTED
//            granted = granted and hasPermission
//            if (!hasPermission) {
//                permissionsNeeded.add(s)
//            }
//        }
//        return if (granted) {
//            true
//        } else {
//            ActivityCompat.requestPermissions(
//                activity,
//                permissionsNeeded.toTypedArray(),
//                requestCode
//            )
//            false
//        }
//    }
//
//    fun permissionGranted(
//        requestCode: Int, permissionCode: Int, grantResults: IntArray
//    ): Boolean {
//        return requestCode == permissionCode && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED
//    }
//}
