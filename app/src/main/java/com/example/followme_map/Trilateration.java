package com.example.followme_map;


public class   Trilateration {
    double resultX;
    double resultY;
    BeaconData b1;
    BeaconData b2;
    BeaconData b3;

    Trilateration(BeaconData b1,  BeaconData b2,  BeaconData b3) {
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

//        System.out.printf("Cal - %f,%f,%f,%f,%f,%f,%f,%f,%f\n", x1,y1,r1,x2,y2,r2,x3,y3,r3);

        double A = 2*x2 - 2*x1;
        double B = 2*y2 - 2*y1;
        double C = Math.pow(r1,2) - Math.pow(r2, 2) - Math.pow(x1,2) + Math.pow(x2,2) - Math.pow(y1,2) + Math.pow(y2,2);

        double D = 2*x3 - 2*x2;
        double E = 2*y3 - 2*y2;
        double F = Math.pow(r2,2) - Math.pow(r3,2) - Math.pow(x2,2) + Math.pow(x3,2) - Math.pow(y2,2) + Math.pow(y3,2);

        resultX = (C*E - F*B) / (E*A - B*D);
        resultY = (C*D - A*F) / (B*D - A*E);
    }
}