# <img src="https://user-images.githubusercontent.com/64718800/111408689-fd166400-8718-11eb-8c19-fc7a93d9beeb.png" width="120"> ヨンジン専門大学校 卒業プロジェクト 
<p>
    Beaconを利用してリアルタイムで大型病院の室内にいるユーザーの位置を探索し、診療動線を段階的に案内するOne-StopService「FollowMe」
</p> 

--- 

# 目次
> [1. サービス紹介](#サービス紹介)   
[2. Sequence Diagram](#Sequence-Diagram)   
[3. 使用技術](#使用技術)   
[4. system diagram](#使用技術)   
[5. 開発期間](#開発期間)  
> ## 担当分野   
> > [6. DataBase 設計](#DataBase-設計)    
> > [7. API 種類](#API-種類)      
---
# サービス紹介
> ## サービス名
> > - Follow Me(信じてついて来てくださいという意味)
> ## サービス要約
> >- 本プロジェクトは、大型病院で診療室や検査室が多いため道を探すのが難しい問題点を解決するために、One-StopServiceである「Follow Me」を企画
> >- 「Follow Me」は、大型病院を基にリアルタイムで患者の位置を探索し、医療スタッフが設定した診療動線を段階的に案内する
> >- QRコードを通じた診療受付とアプリ決済など様々なサービスを通して便利で効率的な診療過程を受けることができる
> >- 本プロジェクトにより、初めて病院を訪れる患者や来院者の方にとって、道に迷う不便を軽減することができる
> ## サービス背景
>   > 1. 大型病院で道に迷い
>   >       -  病院の規模が大きいほど診療室や検査室が多く、なかなか見つかりにくい
>   >       -  大手病院で案内板だけに頼って道を探すのは困難
>   >  2. 受付の混乱と会計待ちによる時間の浪費
>   >       - 受付に待機者が多く、お客様の待ち時間が増加
>   >       - 診療を終えた後、収納窓口を訪問すると時間が無駄
> ## サービス流れ
>   > <p align="center"><img src="https://user-images.githubusercontent.com/53847348/110759162-a7613800-8290-11eb-9547-4aaee03b8c91.png" width="1000"></p>
>  ## サービス流れ　アプリ画面
>> <img src="https://user-images.githubusercontent.com/53847348/110759181-ac25ec00-8290-11eb-8879-d0717c8ae9c6.png" width="100%">
>   ## コアサービス
> > - ### 診療動線案内
> >     - 大型病院で診療室、検査室などの病院内の目的地を医療陣が設定した診療過程に従って順次道案内を行うサービス
> >     - サービスプロセス
> >         1. 医療陣は、本プロジェクトで独自に提供する病院受付プログラムを通じて、診療過程による患者の診療順序を設定
> >         2. 患者はアプリで設定された動線を室内地図上で最短経路で道案内を受ける
> >         3. ガイド通知ウィンドウで簡単に探すことができ、もし経路を離脱した場合はパスを再検索
> >         4. 目的地周辺に到着した場合は案内終了
> >         5. すべての診療が終わるまで、（2 ~ 4）の過程を繰り返す
> > - ### QRコード受付
> >     - アプリでQRコードを発行して、診療を受け付けるサービス
> >     - サービスプロセス
> >         1. 患者はアプリに加入し、診療受付のためのQRコードを発行
> >         2. 診療室にあるQRコードスキャナー(医療スタッフアプリ)にQRコードを認識させて診療を受付
> >         3. 診療受付完了後、診療待機番号確認
> > - ### 決済システム
> >     - 商用決済システムを通じてアプリで決済できるサービス

# Sequence-Diagram
>   <p align="center"><img src="https://user-images.githubusercontent.com/64718800/111408992-71e99e00-8719-11eb-879f-da79aee4c229.png" width="1300">
</p>

# 使用技術
> <img src="https://user-images.githubusercontent.com/64718800/111477035-10095280-8772-11eb-9bca-b0beda7452c9.JPG" width="80%">
--- 
# System Diagram
> <img src="https://user-images.githubusercontent.com/64718800/111477514-88701380-8772-11eb-857c-9a273dc327af.JPG" width="80%">
# 開発期間
> 企画期間 : 2020-09-11 ~ 2020-10-31   
> 開発期間 : 2020-11-01 ~ 2021-03-02

---


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
- https://developers.google.com/android/reference/com/google/android/gms/maps/OnMapReadyCallback
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

#### QR코드
- https://park-duck.tistory.com/108

#### 기타
- https://developer.android.com/guide/topics/ui/controls/spinner?hl=ko#java
- https://patents.google.com/patent/KR20070013526A/ko
- https://qastack.kr/gis/11409/calculating-the-distance-between-a-point-and-a-virtual-line-of-two-lat-lngs


