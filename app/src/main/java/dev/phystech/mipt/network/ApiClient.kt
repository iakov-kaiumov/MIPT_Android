package dev.phystech.mipt.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.grapesnberries.curllogger.CurlLoggerInterceptor
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory
import io.reactivex.rxjava3.core.Observable
import okhttp3.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import dev.phystech.mipt.models.api.*
import dev.phystech.mipt.repositories.UserRepository
import dev.phystech.mipt.models.api.UsersResponseModel
import java.util.*

class ApiClient private constructor() {

    interface Api {
        @FormUrlEncoded
        @POST("api/oauth/get-token")
        fun getToken(
            @Field("code") code: String,
            @Field("client_id") client_id: String = "2",
            @Field("client_secret") clientSecret: String = CLIENT_SECRET,
            @Field("redirect_uri") redirectUri: String = "appmipt",
            @Field("grant_type") grant_type: String = "authorization_code"
        ): Observable<AuthorizationModel>

        @GET("api/history")
        fun getHistory(@Query("filter[language.id][]") lang: Int = getLanguageID()): Observable<HistoryDataResponseModel>

        @GET("api/history")
        fun getHistoryById(@Query("filter[id]") id: String): Observable<HistoryDataResponseModel>

        @GET("api/news")
        fun getNews(@Query("filter[language.id][]") lang: Int = getLanguageID()): Observable<NewsDataResponseModel>

        @GET("api/event")
        fun getEvents(
            @Query("page") page: Int = 1,
            @Query("filter[language.id][]") lang: Int = getLanguageID(),
            @QueryMap filterChairMap: Map<String, String>? = null
        ): Observable<EventDataResponseModel>

        @GET("api/news")
        fun getNewsForCategory(
            @Query("filter[eventCategory]") category: String,
            @Query("page") page: Int = 1,
            @Query("filter[language.id][]") lang: Int = getLanguageID()
        ): Observable<EventDataResponseModel>

        @GET("api/chair")
        fun getChair(
//            @Query("filter[language.id][]") lang: Int = getLanguageID(),
            @Query("filter[0posts.id]") filterPost: Int = 0,
            @Query("perpage") perPage: Int = 1000
        ): Observable<ChairDataResponseModel>

        @GET("api/place")
        fun getPlaces(
            @Query("filter[language.id][]") lang: Int = getLanguageID(),
            @Query("perpage") perPage: Int = 1000
        ): Observable<PlacesDataResponseModel>

        @GET("api/place")
        fun getPlaceById(
            @Query("filter[id][]") id: String
//            , @Query("filter[language.id][]") lang: Int = getLanguageID()
        ): Observable<PlacesDataResponseModel>

        @GET("api/phonebook-contact")
        fun getContacts(
            @Query("perpage") itemInPage: Int = 1000
        ): Observable<ContactsDataResponseModel>

        @GET("api/phonebook-contact")
        fun getDeletedContacts(
            @Query("perpage") itemInPage: Int = 10000,
            @Query("filter[status][]") filter: Int = -1
        ): Observable<ContactsDataResponseModel>

        @GET("api/schedule/my")
        fun getSchedulers(): Observable<SchedulersDataResponseModel>

        @GET("api/meet-file")
        fun getMeetFiles(@Query("filter[language.id][]") lang: Int = getLanguageID()): Observable<MeetFilesResponseModel>

        @GET("api/place-tag")
        fun getTagsById(
            @Query("filter[id]") id: String? = null,
            @Query("filter[language.id][]") lang: Int = getLanguageID()
        ): Observable<TagsDataResponseModel>

        @GET("api/bot-answer")
        fun getChatAnswer(@Query("q") question: String): Observable<ChatDataResponse>

        @GET("api/qr-code")
        fun qrCodeValues(@Query("filter[code][]") code: String): Observable<QRCodeDataResponse>

        @FormUrlEncoded
        @POST("api/support/question/add")
        fun sendSupportQuestion(@Field("support-question-form[question]") question: String): Observable<BaseApiEntity>

        @GET("api/users/view")
        fun getUserInfo(): Observable<UserDataResponseModel>

        @GET("api/chair")
        fun getChairTopic(
            @Query("filter[0posts.id]") postId: Int = 0,
            @Query("perpage") itemInPage: Int = 1000
        ): Observable<ChairTopicDataResponseModel>

        @GET("api/setting/view/first-odd-week-start-date")
        fun getFirstOddWeek(): Observable<BaseWithValue>

        @GET("api/users/teachers")
        fun getTeachers(
            @Query("perpage") itemInPage: Int = 2000,
            @Query("filter[>updated]") lastUpdateFrom: String? = null
        ): Observable<TeachersResponseModel>

        @GET("api/users/teachers")
        fun getTeacher(
            @Query("filter[id][]") id: String,
            @Query("perpage") itemInPage: Int = 2000,
            @Query("filter[>updated]") lastUpdateFrom: String? = null
        ): Observable<TeachersResponseModel>

        @GET("api/schedule/time-slots")
        fun getTimeSlots(): Observable<TimeSlotsResponseModel>

        @GET("api/schedule-place")
        fun getSchedulePlace(@Query("perpage") perpage: Int = 2000): Observable<SchedulePlacesResponseModel>

        @FormUrlEncoded
        @POST("api/schedule/update")
        fun updateScheduler(
            @Field("data") data: String,
            @Field("group") group: Int? = null
        ): Observable<UpdateSchedulerResponseModel>

        @FormUrlEncoded
        @POST("api/schedule/create")
        fun createScheduler(
            @Field("data") data: String,
            @Field("group") group: Int? = null
        ): Observable<UpdateSchedulerResponseModel>

        @GET("api/schedule/index")
        fun getScheduleIndex(
            @Query("filter[id][]") id: String? = null,
            @Query("filter[name]") name: String? = null,
            @Query("filter[weekday][]") weekday: String? = null,
            @Query("filter[course.semesters.id]") course: String? = null,
            @Query("filter[type]") type: String? = null,
            @Query("filter[hour][]") hour: String? = null,
            @Query("filter[places.id][]") places: String? = null,
            @Query("filter[teachers.id][]") teachers: String? = null,
            @Query("filter[course.id][]") courseId: Int? = null,
            @Query("perpage") perpage: Int = 1000
        ): Observable<SchedulerIndexResponseModel>

        @GET("api/schedule/subscribe/{id}")
        fun schedulerSubscribe(
            @Path("id") id: String,
            @Query("secret") secret: String
        ): Observable<BaseApiEntity>

        @GET("api/schedule/unsubscribe/{id}")
        fun schedulerUnsubscribe(@Path("id") id: String): Observable<BaseApiEntity>

        @GET("api/schedule-place")
        fun getSchedulePlace(@Query("filter[id][]") placeId: String): Observable<SchedulePlacesResponseModel>

        @GET("api/schedule-event/my")
        fun getMyEvents(): Observable<MyEventResponse>

        @GET("api/schedule-event/index")
        fun getSchedulerEvent(@Query("filter[id][]") id: Int): Observable<EventResponse>

        @FormUrlEncoded
        @POST("api/schedule-event/update")
        fun updateSchedulerEvent(
            @Field("data") data: String,
            @Field("group") group: Int? = null
        ): Observable<UpdateSchedulerEventResponseModel>

        @FormUrlEncoded
        @POST("api/schedule-event/create")
        fun createSchedulerEvent(
            @Field("data") data: String,
            @Field("group") group: Int? = null
        ): Observable<UpdateSchedulerEventResponseModel>

        @GET("api/schedule-event/unsubscribe/{id}")
        fun eventUnsubscribe(@Path("id") id: String): Observable<BaseApiEntity>

        @GET("api/schedule-event/subscribe/{id}")
        fun eventSubscribe(
            @Path("id") id: String,
            @Query("secret") secret: String?
        ): Observable<BaseApiEntity>

        @GET("api/schedule/reset/{id}")
        fun resetChanges(@Path("id") schedulerId: Int): Observable<UpdateSchedulerResponseModel>

        @GET("api/schedule-event/reset/{id}")
        fun resetEventChanges(@Path("id") eventId: Int): Observable<UpdateSchedulerEventResponseModel>

        @GET("api/schedule-course")
        fun getSportSections(
            @Query("filter[type][]") type: String = "section",
            @Query("perpage") perpage: Int? = 1000
        ): Observable<SportSectionResponseModel>

        @GET("api/schedule-course")
        fun getScheduleCategories(
            @Query("filter[type][]") type: String = "category",
            @Query("perpage") perpage: Int? = 100
        ): Observable<SportSectionResponseModel>

        @FormUrlEncoded
        @POST("api/schedule-place/add")
        fun createSchedulerPlace(
            @Field("schedule-place-form[schedule-place][name]") name: String?,
            @Field("schedule-place-form[schedule-place][type]") type: String?,
            @Field("schedule-place-form[schedule-place][building]") building: String?,
            @Field("schedule-place-form[schedule-place][floor]") floor: String?
        ): Observable<SchedulerAddResponseModel>

        @GET("api/chair?perpage=MAX_ITEMS&filter[0posts.id]=0")
        fun getChairsList(
            @Query("filter[language.id][]") lang: Int = getLanguageID()
        ): Observable<ChairDataResponseModel>

        @GET("api/chair?filter[posts.type][]=event&perpage=MAX_ITEMS")
        fun getEventChairsList(
            @Query("filter[language.id][]") lang: Int = getLanguageID()
        ): Observable<ChairDataResponseModel>

        @GET("api/news")
        fun getNewsFiltered(
            @QueryMap chairs: Map<String, String>,
            @Query("perpage") perPage: Int = 20,
            @Query("page") page: Int = 1,
            @Query("filter[language.id][]") lang: Int = getLanguageID()
        ): Observable<EventDataResponseModel>

        @GET("api/users/login/{id}")
        fun login(@Path("id") id: Int): Observable<UserLoginResponseModel>

        @GET("api/setting/view/android-version")
        fun androidVersion(): Observable<VersionResponseModel>

        @GET("api/users")
        fun getUsers(
            @Query("filter[fullname]") fullname: String? = null,
            @Query("perpage") perPage: Int = 20
        ): Observable<UsersResponseModel>

        @GET("api/message/get-conversations")
        fun getConversations(
            @Query("type") type: Int? = null,
            @Query("limitnum") limitnum: Int? = 101,
            @Query("limitfrom") limitfrom: Int? = 0
        ): Observable<ChatResponse>

        @GET("api/message/get-conversation-counts")
        fun getConversationsCount(): Observable<ChatCountsResponse>

        @GET("api/message/get-blocked-users")
        fun getBlockedUsers(): Observable<BlockUsersResponse>

        @GET("api/message/block-user")
        fun blockUser(
            @Query("blockeduserid") id: String
        ): Observable<Void>

        @GET("api/message/unblock-user")
        fun unblockUser(
            @Query("unblockeduserid") id: String
        ): Observable<Void>

        @GET("api/message/get-conversation-messages")
        fun getChat(
            @Query("convid") convid: Long,
            @Query("newest") newest: Int = 1,
            @Query("limitnum") limitnum: Int,
            @Query("limitfrom") limitfrom: Int
        ): Observable<ChatDetailResponse>

        @GET("api/message/mark-all-conversation-messages-as-read")
        fun markAllMessagesAsRead(
            @Query("conversationid") convid: Long
        ): Observable<Void>

        @POST("api/message/send-messages-to-conversation")
        fun sendMessage(
            @Query("conversationid") convid: Long,
            @Body body: ChatSendMessageRequest
        ): Observable<ChatSendMessageResponse>

        @POST("api/message/send-instant-messages")
        fun sendMessageNewChat(
            @Body body: ChatSendMessageRequest
        ): Observable<ChatSendMessageResponse>

        @GET("api/message/delete-message")
        fun deleteMessage(
            @Query("messageid") messageid: Long
        ): Observable<ChatSendMessageResponse>

        @GET("api/message/delete-conversations-by-id")
        fun deleteDialog(
            @Query("conversationids[]") convid: Long
        ): Observable<ChatSendMessageResponse>

        @GET("api/message/delete-conversations-by-id")
        fun deleteChat(
            @Query("conversationids[]") convid: Long
        ): Observable<ChatSendMessageResponse>

        @GET("api/message/get-conversation-members")
        fun getGroupChatUsers(
            @Query("conversationid") convid: Long
        ): Observable<ChatCgoupMembersResponse>

        @GET("api/message/get-member-info")
        fun getUserInfo(
            @Query("userids[]") id: String,
            @Query("includecontactrequests") includecontactrequests: Int = 1,
            @Query("includeprivacyinfo") includeprivacyinfo: Int = 1
        ): Observable<UserInfoResponse>

        @FormUrlEncoded
        @POST("api/oauth/add-external-token")
        fun sendDeviceToken(
            @Field("access_token") accessToken: String,
            @Field("type") type: String = "android"
        ): Observable<BaseApiEntity>

        @GET("api/oauth/logout")
        fun logout(): Observable<BaseApiEntity>

        @GET("api/message/get-user-message-preferences")
        fun getUserMessagePreferences(): Observable<SettingsChatNotificationsResponse>

        @GET("api/message/set-user-message-preferences")
        fun setUserMessagePreferences(
            @Query("emailNotify") emailNotify: Int
        ): Observable<SettingsChatNotificationsResponse>

        @GET("api/users")
        fun getPsychologists(
            @Query("filter[roles.role.name]") filter: String = "psychologist",
            @Query("page") page: Int = 1,
            @Query("perpage") perPage: Int = 1000
        ): Observable<UsersResponseModel>

        @GET("api/psy-event")
        fun getPsychologistsTime(
            @Query("filter[free][]") filter: Int = 1,
            @Query("page") page: Int = 1,
            @Query("perpage") perPage: Int = 1000,
            @Query("filter[psy.id][]") psyId: String? = null,
            @Query("filter[location][]") location: String? = null,
            @Query("filter[type][]") type: String = "online"
        ): Observable<PsyTimeResponseModel>

        @GET("api/psy-event/apply/{id}")
        fun applyPsychologist(
            @Path("id") id: String,
            @Query("phone") phone: String
        ): Observable<PsyApplyResponseModel>
    }


    companion object {

        const val HARD_CODE_TOKEN: String = "MjYtMi0zLTRjOGUwYTE5YTA4N2NjNTUyYTE5MmJjMTJiNDMwNTIxNWYzODkwMDgzNzU3NmQwNTkwZTI0MzdlY2NlYTA4ZmU="
        const val CLIENT_SECRET = "H4TVZdsf4CJmfgveDfs2"

        var shared: Api
            private set

        fun getLanguageID(): Int {
            if (Locale.getDefault().language == "ru") return 1
            else return 2
        }


        init {
            val client = OkHttpClient.Builder()
                .addInterceptor(CurlLoggerInterceptor("CURL"))
                .addNetworkInterceptor {
                    val requestBuilder = it.request().newBuilder()
                        .header("Accept", "application/json")

                    UserRepository.shared.getToken()?.let { token ->
                        requestBuilder.header("Authorization", "Bearer $token")
                    }
                    if (it.request().url.toString().contains("android-version")) {
                        requestBuilder.header("Authorization", "Bearer $HARD_CODE_TOKEN")
                    }
                    it.proceed(requestBuilder.build())
                }.build()


            shared = Retrofit.Builder()
                .baseUrl("https://appadmin.mipt.ru")
                .client(client)
                .addConverterFactory(
                    GsonConverterFactory.create(
                        GsonBuilder().setLenient().create()
                    )
                )
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(Api::class.java)
        }


        fun updateClient() {
            val client = OkHttpClient.Builder()
                .addInterceptor(CurlLoggerInterceptor("CURL"))
                .addNetworkInterceptor {
                    val requestBuilder = it.request().newBuilder()
                        .header("Accept", "application/json")

                    UserRepository.shared.getToken()?.let { token ->
                        requestBuilder.header("Authorization", "Bearer $token")
                    }

                    it.proceed(requestBuilder.build())
                }.build()


            shared = Retrofit.Builder()
                .baseUrl("https://appadmin.mipt.ru")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(Api::class.java)
        }

    }

}