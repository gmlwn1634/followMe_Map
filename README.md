# <img src="https://user-images.githubusercontent.com/64718800/111408689-fd166400-8718-11eb-8c19-fc7a93d9beeb.png" width="120"> ヨンジン専門大学校 卒業プロジェクト
<p>
    Beaconを利用してリアルタイムで大型病院内にいるユーザーの位置を探索し、診療動線を順次に案内するOne-StopService「FollowMe」
</p>

---

# 目次
> [1. サービス紹介](#サービス紹介)  
> [2. Sequence Diagram](#Sequence-Diagram)  
> [3. 使用技術](#使用技術)  
> [4. system diagram](#使用技術)  
> [5. 開発期間](#開発期間)  
> > 担当した部分
> > > [6. ユーザーアプリ開発](#ユーザーアプリ開発)
---
# サービス紹介
> ## サービス名
> > - Follow Me(信じてついて来てくださいという意味)
> ## サービス要約
> >- 本プロジェクトは、大型病院で行きたい診療室や検査室を探すのが難しい問題点を解決するために、One-StopServiceである「Follow Me」を企画
> >- 「Follow Me」は、大型病院を基にリアルタイムで患者の位置を探索し、医療Staffが設定した診療動線を順次に案内する
> >- QRコードを通じた診療受付とアプリ決済など様々なサービスを通して便利で効率的な診療過程を受けることができる
> >- 本プロジェクトにより、初めて病院を訪れる患者や来院者の方にとって、道に迷う不便を軽減することができる
> ## サービス背景
>   > 1. 大型病院で道に迷い
>   >       -  病院の規模が大きいほど診療室や検査室が多く、なかなか見つかりにくい
>   >       -  大手病院で案内板だけに頼って道を探すのは困難
>   >  2. 受付の混乱と会計待ちによる時間の浪費
>   >       - 受付に待機者が多く、お客様の待ち時間が増加
>   >       - 診療が終わった後、収納窓口を訪問すると時間が無駄
> ## サービス流れ
>   > <p align="center"><img src="https://user-images.githubusercontent.com/53847348/110759162-a7613800-8290-11eb-9547-4aaee03b8c91.png" width="1000"></p>
>  ## サービス流れ　アプリ画面
>> <img src="https://user-images.githubusercontent.com/53847348/110759181-ac25ec00-8290-11eb-8879-d0717c8ae9c6.png" width="100%">
>   ## コアサービス
> > - ### 診療動線案内
> >     - 大型病院で診療室、検査室などの病院内の目的地を医療Staffが設定した診療過程に従って順次道案内を行うサービス
> >     - サービスプロセス
> >         1. 医療Staffは、本プロジェクトで独自に提供する病院受付プログラムを通じて、診療過程による患者の診療順序を設定
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

# ユーザーアプリ開発
![그림1](https://user-images.githubusercontent.com/61926841/118592297-b6102200-b7e0-11eb-941c-d8e923d0b558.png)
![그림2](https://user-images.githubusercontent.com/61926841/118592274-aee91400-b7e0-11eb-984a-d1b1e7dceb1e.png)
![그림3](https://user-images.githubusercontent.com/61926841/118592287-b3adc800-b7e0-11eb-95f6-fe3f057ddf5a.png)
![그림4](https://user-images.githubusercontent.com/61926841/118592291-b4def500-b7e0-11eb-8a4e-1355dd9948d2.png)



---


### 参考したサイト
#### webAPI
- https://documenter.getpostman.com/view/13059220/TVep8nfZ

#### fragment
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
- https://onedaycodeing.tistory.com/26
- https://medium.com/@vdongbin/firebase%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-push-notification-5c8a83932472

#### Volley 通信
- https://developer.android.com/training/volley/simple?hl=ko
- https://ju-hy.tistory.com/66
- https://velog.io/@dlrmwl15/%EC%95%88%EB%93%9C%EB%A1%9C%EC%9D%B4%EB%93%9C-Volley%EB%A5%BC-%EC%9D%B4%EC%9A%A9%ED%95%9C-HTTP-%ED%86%B5%EC%8B%A0
- https://lktprogrammer.tistory.com/175

#### Recycler View
- https://recipes4dev.tistory.com/168
- https://recipes4dev.tistory.com/154

#### QRコード
- https://park-duck.tistory.com/108

#### その以外
- https://developer.android.com/guide/topics/ui/controls/spinner?hl=ko#java
- https://patents.google.com/patent/KR20070013526A/ko
- https://qastack.kr/gis/11409/calculating-the-distance-between-a-point-and-a-virtual-line-of-two-lat-lngs


