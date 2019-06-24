package br.com.mbc.geofirepassageiro;

import androidx.fragment.app.FragmentActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;                                         //Google Mapas - Mapas
    FirebaseDatabase fireDados = FirebaseDatabase.getInstance();    //Firebase - DataBase (Banco de Dados);

    double passLat = -23.562791; double passLon = -46.654668;       //Posição Passageiro;
   // double motLat = -23.563196; double motLong = -46.650607;        //Posição Motorista;    Fora do RAnge       ////////////////////////
    double motLat1 = -23.564270; double motLong1 = -46.652954;        //Posição Motorista;    Dentro do RAnge ////////////////////////////////
    LatLng passageiro = new LatLng(passLat, passLon);
    LatLng motorista = new LatLng(motLat1, motLong1);                                               //////////////////////////////////


    @Override protected void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //LocalPassageiro
        mMap.addMarker(new MarkerOptions().position(passageiro).title("Local Passageiro"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(passageiro));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(passageiro, 15.00f));        //Zoom do Mapa/Marcador (2.0 a 21.0)

        //Local Motorista
        mMap.addMarker(new MarkerOptions().position(motorista).title("Local Motorista"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(motorista));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(motorista, 15.00f));        //Zoom do Mapa/Marcador (2.0 a 21.0)

        //Referencia Firebase
        localPassageiro(passLat, passLon);
        localMotorista(motLat1, motLong1);                                                          //////////////////////////////////////////
        monitoramentoGeofire(passLat, passLon, motLat1, motLong1 );                                 //////////////////////////////////////////
    }

    //Atualizar Localização do Passageiro
    public void localPassageiro(double lat, double lon){
        //Local do Firebase
        DatabaseReference localPassageiro = fireDados.getReference("LocalizacaoUsuario");
        localPassageiro.child("latitude").setValue(lat);
        localPassageiro.child("latitude").setValue(lon);

        //Salvar no Firebase
        GeoFire geoFire = new GeoFire(localPassageiro);
        geoFire.setLocation("idPassageiro",
                new GeoLocation(lat, lon),
                new GeoFire.CompletionListener() {
                    @Override public void onComplete(String key, DatabaseError error) {
                        if(error != null){
                            Log.d("myLog", "Erro ao Salvar Usuário");
                        }
                    }
                }
        );
    }

    //Atualizar Localização do Motorista
    public void localMotorista(double lat, double lon){
        //Local do Firebase
        DatabaseReference localMotorista = fireDados.getReference("LocalizacaoUsuario");
        localMotorista.child("latitude").setValue(lat);
        localMotorista.child("latitude").setValue(lon);

        //Salvar no Firebase
        GeoFire geoFire = new GeoFire(localMotorista);
        geoFire.setLocation("idMotorista",
                new GeoLocation(lat, lon),
                new GeoFire.CompletionListener() {
                    @Override public void onComplete(String key, DatabaseError error) {
                        if(error != null){
                            Log.d("myLog", "Erro ao Salvar Usuário");
                        }
                    }
                }
        );
    }

   //Biblioteca Geofire
    public void monitoramentoGeofire (double passLat, double passLon, double motLat, double motLong){
        //Iniciar GeoFire
        DatabaseReference localUsuario = fireDados.getReference("LocalizacaoUsuario");
        GeoFire geoFire = new GeoFire(localUsuario);

        //Range para o GeoFire Avisar (pode adicionar ou não um círculo deste range)
        Circle circulo = mMap.addCircle(
                new CircleOptions()
                .center(passageiro)
                .radius(350)    //em metros
                .fillColor(Color.argb(90, 255, 153, 0))
                .strokeColor(Color.argb(190, 255, 153, 0))
        );

        //Geofire Range
        GeoQuery geoQuery = geoFire.queryAtLocation(
                new GeoLocation(passLat, passLon),
                0.35   //em km (0.1 = 100 metros. 0.05 = 50 metros
        );

        //GeoFire Listener (monitorar os ranges)
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            //Quando Der Range
            @Override public void onKeyEntered(String key, GeoLocation location) {
                if(key.equals("idPassageiro")){                                         //no exemplo foi utilizado o id do passageiro (Get push)
                    Log.d("myLog", "Passageiro esta na Área");
                } else if(key.equals("idMotorista")){
                    Log.d("myLog", "Motorista esta na Área");
                }
            }

            //Quando Sair do Range
            @Override public void onKeyExited(String key) {

            }

            //Quando os Marcadores Se Movimentam (mas continuam no range)
            @Override public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override public void onGeoQueryReady() {

            }

            @Override public void onGeoQueryError(DatabaseError error) {

            }
        });


    }



}

//falta ver como centralizar os dois marcadores!!!!!!!!!!!!! (tem no jamilton)

//Dúvidas
//Por que um Projeto com o Maps Activity esta dando erro?
//circulo e o geofire tem que ser o mesmo valor (mas um em metros e outro em km) ???

//Processo ( Usar 2 Projetos )
//1.Criar Projeto com Maps Activity;
//2.Arrumar o Erro (Refactor / Migrate to AndroidX). Atualizar as Bibliotecas do Gradle;
//3.Criar Chave de Api da Google (Credenciais / Chave de API / Restringir Chave / Restringir Chave);
//4.Colocar a Chave em Google_Maps_api.xml (res / values);
//5.Adicionar o Firebase no Projeto (Autent e Database)  -  conta = marcelobaldi250@gmail.com   e  projeto = ReconhecerCaracter
//6.Colocar a Posição Atual do Passageiro no Projeto/Emulador do Passageiro e Marcador no Mapa;
//7.Colocar a Posição Atual do Motorista no Projeto/Emulador do Motorista e Marcador no Mapa;

//Emulador com Playstore (logado na conta google e Atualizado);

//https://github.com/firebase/geofire-java