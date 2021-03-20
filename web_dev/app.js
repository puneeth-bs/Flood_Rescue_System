var firebaseConfig = {
    apiKey: "AIzaSyDSmHLO48eQvr8i6f-e9Bj0dmGlqlWkLxw",
    authDomain: "disastermangement-1de94.firebaseapp.com",
    databaseURL: "https://disastermangement-1de94-default-rtdb.firebaseio.com",
    projectId: "disastermangement-1de94",
    storageBucket: "disastermangement-1de94.appspot.com",
    messagingSenderId: "366752650310",
    appId: "1:366752650310:web:ab01a77a8f22827320e647",
    measurementId: "G-Q47TZ3RH7Y"
  };
  // Initialize Firebase
  firebase.initializeApp(firebaseConfig);


  let loc = firebase.database().ref("flask_app");

  loc.on('value',gotdata,errdata);

  function gotdata(data)
{
  var scores = data.val();
  var keys = Object.keys(scores);
  let res = document.querySelector(".result");
  res.innerHTML="";


  console.log(0, scores[0])
  for(var i =0;i< keys.length;i++)
  {
    var j =i+1;
    var k =keys[i];
    console.log(k, scores[k].Name)
    var msg = scores[k].Designation;
    var msg1 = scores[k].Name;
    var loc = scores[k].Location;

    var bl = scores[k].blood_group;
    // var lat = loc.slice()
   console.log(i, scores[k].Location);
    res.innerHTML += `<div>
    <p>${j+". "+msg1+"<br />"+msg+"<br />"+loc+"<br/>"+"Blood group: "+bl}<p>
    </div>`;


  }

  
  var k1=keys[0];
  
  var loc1 = scores[k1].Location;
  var aall = loc1.slice(10,30);
  var aall1 = aall.split(",");
  console.log(aall1[0],aall1[1]);
  const mymap = L.map('mymap').setView([aall1[0], aall1[1]], 13)
  const attribution =
    '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors';
  const tileUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png';
  const tiles = L.tileLayer(tileUrl, { attribution });
  tiles.addTo(mymap);
  for(var i =0;i< keys.length;i++)
  {
    var j =i+1;
    var k =keys[i];
      var msg = scores[k].Designation;
    var loc = scores[k].Location;
    var msg1 = scores[k].Name;

    var as = loc.slice(10,30);
    var as1 = as.split(",");


    marker = new L.marker([as1[0],as1[1]])
   .addTo(mymap);
   var icon = marker.options.icon;
   // console.log(icon.options.iconSize);
icon.options.iconSize = [18, 30];
marker.setIcon(icon);
   marker.bindPopup(msg1+"<br />"+msg);
          marker.on('mouseover', function (e) {
              this.openPopup();
          });
          marker.on('mouseout', function (e) {
              this.closePopup();
          });




  }


  var c = [],i,f=1,l=1;

   for (i = 0; i < keys.length ; i += 1) {
     var k =keys[i];
       var msg = scores[k].Designation;


       if (msg === "Rescue ops" && f>0) {
         var loc = scores[k].Location;

         var as = loc.slice(10,30);
          var as1 = as.split(",");
          var len = as1.length;
          as1[1]=as1.slice(1,len);
          var a = as1[0];
          var b = as1[1];
         var a= parseFloat(a);
         var b = parseFloat(b);

           c.push([a,b]);
           console.log([a,b]);
           f=f-1;
       }
       if (msg === "Survivor" && l>0) {
         var loc = scores[k].Location;

         var as = loc.slice(10,30);
         var as1 = as.split(",");
         var len = as1.length;
         as1[1]=as1.slice(1,len);
         var a = as1[0];
         var b = as1[1];
        var a= parseFloat(a);
        var b = parseFloat(b);

          c.push([a,b]);
          console.log([a,b]);
           l=l-1;
       }

   }
   L.polyline(c).addTo(mymap);

}







// marker.bindPopup("Popup content");
//        marker.on('mouseover', function (e) {
//            this.openPopup();
//        });
//        marker.on('mouseout', function (e) {
//            this.closePopup();
//        });
//






  function errdata(err)
  {
    console.log('error!');
    console.log(err);
  }
