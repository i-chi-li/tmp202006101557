package hello

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import javax.xml.bind.annotation.XmlRootElement

@Controller("/hello")
class HelloController(
    private val xmlMapper: XmlMapper
) {
    @Get("/{?params*}")
    @Produces(MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON)
    fun get(
        request: HttpRequest<*>,
        params: RequestBody?
    ): HttpResponse<*> {
        println("params: $params")
        val responseBodyData = ResponseBodyData(
            time = 123456789,
            result = "10",
            errors = listOf(
                ResponseBodyError(
                    code = "error code",
                    message = "error message"
                )
            ),
            events = listOf(
                ResponseBodyEvent(
                    id = "id01",
                    requestDatetime = "2020/06/10 10:20:30"
                )
            )
        )
        val responseBody = if (request.isXmlResponse()) {
            // XML 形式レスポンスの場合
            responseBodyData
        } else {
            // JSON 形式レスポンスの場合
            ResponseBody(responseBodyData)
        }
        return HttpResponse.ok(responseBody).apply {
            contentType(
                request.headers
                    .accept()
                    .firstOrNull() ?: MediaType.APPLICATION_XML_TYPE
            )
        }
    }

    @Post
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
    fun postForm(request: HttpRequest<RequestBody>): HttpResponse<*> {
        val body = request.body.orElse(null)
        return HttpResponse.ok(body)
    }
}

/**
 * リクエストボディ
 *
 * @property request
 */
@Introspected
data class RequestBody(
    val request: RequestBodyData = RequestBodyData()
)

@Introspected
@XmlRootElement(name = "request")
data class RequestBodyData(
    val version: String? = null,
    @field:JsonProperty("shop_id")
    val shopId: String? = null
)

/**
 * XML 形式レスポンスの場合は、true を返す。
 *
 * レスポンスは、Accept ヘッダの最初に application/json を
 * 指定したもの以外は、XML 形式のレスポンスとする。
 *
 * @return
 */
fun HttpRequest<*>.isXmlResponse(): Boolean {
    val acceptType = this.headers
        .accept()
        .firstOrNull()
        ?: MediaType.APPLICATION_XML_TYPE
    println("acceptType: $acceptType")
    return acceptType == MediaType.APPLICATION_XML_TYPE
}

/**
 * レスポンスボディ
 *
 * @property response
 */
@Introspected
data class ResponseBody(
    val response: ResponseBodyData = ResponseBodyData()
)

@Introspected
@XmlRootElement(name = "response")
data class ResponseBodyData(
    val time: Long? = null,
    val result: String? = null,
    @JacksonXmlElementWrapper(localName = "errors")
    @JacksonXmlProperty(localName = "error")
    val errors: List<ResponseBodyError> = listOf(),
    @JacksonXmlElementWrapper(localName = "events")
    @JacksonXmlProperty(localName = "event")
    val events: List<ResponseBodyEvent>? = null
)

@Introspected
data class ResponseBodyError(
    val code: String,
    val message: String? = null
)

@Introspected
data class ResponseBodyEvent(
    val id: String? = null,
    @JsonProperty("request_datetime")
    val requestDatetime: String? = null
)
