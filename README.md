


# followMe_Map
    병원 실시간 길안내 프로젝트


### 참고사이트
#### webAPI
- https://documenter.getpostman.com/view/13059220/TVep8nfZ

#### fragment
    프래그먼트는 하나의 액티비티가 여러 개의 화면을 가지도록 만들기위해 고안된 개념
    다양한 크기의 화면을 가진 모바일 환경이 늘어남에 따라 하나의 디스플레이 안에서 다양한 화면 요소들을 보여주기 위해 생김

- https://tedrepository.tistory.com/5
- https://www.youtube.com/watch?v=3Th96mVEpyo

#### googleMap
- https://developers.google.com/android/reference/com/google/android/gms/maps/GoogleMap?hl=ko
- https://developers.google.com/android/reference/com/google/android/gms/maps/OnMapReadyCallback"
- https://developers.google.com/maps/documentation/android-sdk/events
- https://developers.google.com/android/reference/com/google/android/gms/maps/model/CameraPosition?hl=ko
- https://developers.google.com/maps/documentation/android-sdk/marker
- https://developers.google.com/maps/documentation/android-sdk/groundoverlay
- https://stackoverrun.com/ko/q/4445784
- https://breadboy.tistory.com/287
- https://developers.google.com/android/reference/com/google/android/gms/maps/model/PolylineOptions

#### googleMap Compass
- https://developer.android.com/guide/topics/sensors/sensors_position?hl=ko#java
- https://stackoverflow.com/questions/50996340/android-getorientation-description-does-not-match-output-values
- http://androstudio77.blogspot.com/p/blog-page_13.html
- https://biig.tistory.com/36
- https://copycoding.tistory.com/116
- https://swlock.blogspot.com/2017/10/android-cameracompass.html
- https://developer.android.com/guide/topics/sensors/sensors_position?hl=ko
- https://stackoverflow.com/questions/33877621/how-to-detect-movecamera-is-finished-on-the-google-map-service/33878134

#### thread
- https://recipes4dev.tistory.com/150
- https://mine-it-record.tistory.com/246
- https://itmining.tistory.com/6
- https://itmining.tistory.com/4
- https://velog.io/@dlrmwl15/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-%EC%8A%A4%EB%A0%88%EB%93%9CThread%EC%99%80-%ED%95%B8%EB%93%A4%EB%9F%ACHandler

#### FCMtoken
    FCM은 앱의 키값을 식별하여 특정 스마트폰에 데이터를 전달할 수 있다.
    앱의 고유한 키 값은 Firebase에 앱을 등록하면 생성된다.
    사용자가 이 앱을 설치하고 알람설정에 동의한다면, 기기의 FCM token을 얻을 수 있다.
    FCM의 Push는 앱의 고유한 키 값과 각 사용자들의 token을 통해 이루어진다.
- https://onedaycodeing.tistory.com/26
- https://medium.com/@vdongbin/firebase%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-push-notification-5c8a83932472

##### Socket 통신
    서버와 클라이언트가 계속 연결을 유지하는 양방향 통신으로 IP와 포트 번호로 소켓을 연결하여 통신한다.
    양쪽 어플리케이션 모두 데이터 요청/응답 가능
    계속 연결을 유지하는 연결지향형 통신으로 실시간 통신이 필요한 경우 사용한다.
    채팅, 온라인 게임, 실시간 동영상 강좌, 문서 공동 작업 등에서 사용
    
- https://blog.opid.kr/302
- https://bangsj1224.tistory.com/entry/5-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%82%B9-%EC%86%8C%EC%BC%93-%EC%82%AC%EC%9A%A9%ED%95%98%EA%B8%B0
- https://seopseop911.tistory.com/15
- https://m.blog.naver.com/PostView.nhn?blogId=rhrkdfus&logNo=221406915879&proxyReferer=https:%2F%2Fwww.google.com%2F


#### Socket.io 
    실시간으로 상호작용하는 웹 서비스를 만드는 기술인 웹 소켓을 기반으로 클라이언트와 서버의 양방향 통신을 가능하게 해주는 모듈
    클라이언트가 웹 소켓 프로토콜로 서버에 통신을 보내면, 서버는 소켓의 이벤트 발생에 따른 응답을 클라이언트에게 보내준다.
    소켓에 미리 이벤트를 걸어 놓고, 해당 이벤트 발생 시 그에 따른 이벤트 처리
    
-https://socket.io/blog/native-socket-io-and-android/
-https://m.blog.naver.com/PostView.nhn?blogId=mym0404&logNo=221344144643&categoryNo=70&proxyReferer=&proxyReferer=https:%2F%2Fwww.google.com%2F
-http://blog.naminsik.com/3651
-https://dev-juyoung.github.io/2017/09/05/android-socket-io/
-https://github.com/jinusong/Android-Socket

#### Volley 통신
    앱에서 서버와 http 통신을 할 때 HttpURLConnection을 사용하면 직접 요청과 응답을 받는 것이 가능하다. 
    하지만 직접 쓰레드를 구현해야 하며, 기본적인 코드 양 또한 많아 코드가 복잡해진다는 단점이 있다. 
    그래서 안드로이드에서는 쉽고 빠른 http 통신을 위해 Volley 라이브러리를 제공하고 있다.
    사용자가 Request 객체에 요청 내용을 담아 RequestQueue에 추가하기만 하면,
    RequestQueue가 알아서 쓰레드를 생성하여 서버에 요청을 보내고 응답을 받는다.
    응답이 오면 RequestQueue에서 Request에 등록된 ResponseListener로 응답을 전달해준다.
    따라서 사용자는 별도의 쓰레드 관리 뿐 아니라 UI 접근을 위한 handler 또한 다룰 필요가 없다.

- https://developer.android.com/training/volley/simple?hl=ko
- https://ju-hy.tistory.com/66
- https://velog.io/@dlrmwl15/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-Volley%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-HTTP-%ED%86%B5%EC%8B%A0
- https://lktprogrammer.tistory.com/175

#### Recycler View
- https://recipes4dev.tistory.com/168
- https://recipes4dev.tistory.com/154


#### 기타
- https://developer.android.com/guide/topics/ui/controls/spinner?hl=ko#java
- https://patents.google.com/patent/KR20070013526A/ko
- https://qastack.kr/gis/11409/calculating-the-distance-between-a-point-and-a-virtual-line-of-two-lat-lngs


