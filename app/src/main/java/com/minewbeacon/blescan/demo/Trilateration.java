package com.minewbeacon.blescan.demo;


public class Trilateration {
    boolean doKalman = true;
    double resultX;
    double resultY;
    BeaconData b1;
    BeaconData b2;
    BeaconData b3;

    Trilateration(double preLat, double preLng, BeaconData b1, BeaconData b2, BeaconData b3) {
        double x1,y1,r1,x2,y2,r2,x3,y3,r3;

        x1 = b1.getLat_TM();
        y1 = b1.getLng_TM();
        r1 = b1.getDistance();


        x2 = b2.getLat_TM();
        y2 = b2.getLng_TM();
        r2 = b2.getDistance();

        x3 = b3.getLat_TM();
        y3 = b3.getLng_TM();
        r3 = b3.getDistance();

        switch (b1.group) {
            case "2": // 직선 그래프 + 매핑 (복도)
                double inclination =  (y1-y2)/(x1-x2);  // 기울기
                double d = y1 - inclination*x1 ;        // y절편

                double D1;
                double D2;

                double a = (inclination*inclination+1);
                double b = 2*(inclination*d - inclination*y1 - x1);
                double c = x1*x1-(r1*r1)+y1*y1+d*d-2*d*y1;

                /* 2차 방정식의 판별식 */
                D1 = b * b - 4 * a *c;

                double result_x1 = -b / (2 * a) + Math.sqrt(D1) / (2 * a);
                double result_x2 = -b / (2 * a) - Math.sqrt(D1) / (2 * a);

                //-------------------------------------------------------------
                a = (inclination*inclination+1);
                b = 2*(inclination*d - inclination*y2 - x2);
                c = x2*x2-(r2*r2)+y2*y2+d*d-2*d*y2;
                /* 2차 방정식의 판별식 */
                D2 = b * b - 4 * a * c;

                double result_x3 = -b / (2 * a) + Math.sqrt(D2) / (2 * a);
                double result_x4 = -b / (2 * a) - Math.sqrt(D2) / (2 * a);



                int temp = 0;
                double[] p = new double[4];

                p[0] = Math.abs(Math.pow((result_x1 - result_x3),2));
                p[1] = Math.abs(Math.pow((result_x1 - result_x4),2)) ;
                p[2] = Math.abs(Math.pow((result_x2 - result_x3),2)) ;
                p[3] = Math.abs(Math.pow((result_x2 - result_x4),2)) ;

                for(int i = 0; i < 3; i++) {
                    if(p[temp] > p[i+1]) {
                        temp = i+1;
                    }
                }


                double x = 0;
                double y = 0;

                if(temp == 0) {
                    x = (result_x1 + result_x3)/2;
                    System.out.println("1@@@");
                }

                else if(temp == 1) {
                    x = (result_x1 + result_x4)/2;
                    System.out.println("2@@@");
                }

                else if(temp == 2) {
                    x = (result_x2 + result_x3)/2;
                    System.out.println("3@@@");
                }

                else {
                    x = (result_x2 + result_x4)/2;
                    System.out.println("4@@@");
                }

                y = inclination*x + d;

                System.out.println("기울기 :" + inclination);
                System.out.println("y절펀 :" + d);
                System.out.println("x1, y1, r1 :" + x1 + ", " + y1 + ", " + r1 );
                System.out.println("근 :" + result_x1 + ", " + result_x2);
                System.out.println("x2, y2, r2 :" + x2 + ", " + y2 + ", " + r2 );
                System.out.println("근 :" + result_x3 + ", " + result_x4);



                double inclination2 =  -1/inclination;
                double d2 = preLng-(inclination2*preLat);

                double comX = (d2-d)/(inclination-inclination2);
                double comY = inclination*comX+d;
                System.out.println("수직점 : " + comX +", "+ comY);

                double checkVar = Math.pow((preLat-comX),2) + Math.pow((preLng-comY),2);
                System.out.println(checkVar);
                if (true) {
                    doKalman = false;
                    resultX = (comX + x)/2;
                    resultY = (comY + y)/2;
                }else {
                    resultX = x;
                    resultY = y;
                }
                break;

            case "3": // 삼변측량 (룸)
                double A = 2 * x2 - 2 * x1;
                double B = 2 * y2 - 2 * y1;
                double C = Math.pow(r1, 2) - Math.pow(r2, 2) - Math.pow(x1, 2) + Math.pow(x2, 2) - Math.pow(y1, 2) + Math.pow(y2, 2);

                double D = 2 * x3 - 2 * x2;
                double E = 2 * y3 - 2 * y2;
                double F = Math.pow(r2, 2) - Math.pow(r3, 2) - Math.pow(x2, 2) + Math.pow(x3, 2) - Math.pow(y2, 2) + Math.pow(y3, 2);

                resultX = (C * E - F * B) / (E * A - B * D);
                resultY = (C * D - A * F) / (B * D - A * E);

                double checkVar2 = Math.pow((preLat-resultX),2) + Math.pow((preLng-resultY),2);
                System.out.println(checkVar2);
                if (checkVar2 < 1) {
                    doKalman = true;
                }else {
                    doKalman = false;
                }
                break;
        }
        //System.out.printf("Cal - %f,%f,%f,%f,%f,%f,%f,%f,%f\n", x1,y1,r1,x2,y2,r2,x3,y3,r3);

    }
}