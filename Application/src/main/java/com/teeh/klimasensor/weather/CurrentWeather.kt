package com.teeh.klimasensor.weather

/**
 * Created by teeh on 23.07.2017.
 */

class CurrentWeather {

    var coord: Coordinate? = null
    var weather: List<Weather>? = null
    var base: String? = null
    var main: WeatherValues? = null
    var visibility: Int? = null
    var wind: Wind? = null

    inner class Coordinate {
        internal var lon: Double? = null
        internal var lat: Double? = null
    }

    inner class Weather {
        internal var id: Int? = null
        internal var main: String? = null
        internal var description: String? = null
        internal var icon: String? = null
    }

    inner class WeatherValues {
        internal var temp: Double? = null
        internal var pressure: Double? = null
        internal var humidity: Double? = null
        internal var temp_min: Double? = null
        internal var temp_max: Double? = null
    }

    inner class Wind {
        internal var speed: Double? = null
        internal var deg: Double? = null
    }
}


//        {"coord":{"lon":8.55,"lat":47.37},
//        "weather":[{"id":803,"main":"Clouds","description":"broken clouds","icon":"04d"}],
//        "base":"stations",
//        "main":{"temp":292.91,"pressure":1018,"humidity":56,"temp_min":292.15,"temp_max":293.15},
//                "visibility":10000,
//                "wind":{"speed":4.1,"deg":300},
//                "clouds":{"all":75},
//                "dt":1500828600,
//                "sys":{"type":1,"id":6016,"message":0.0058,"country":"CH","sunrise":1500782041,"sunset":1500836993},
//                "id":2657896,
//                "name":"Zurich",
//                "cod":200}