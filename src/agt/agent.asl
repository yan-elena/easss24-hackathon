!start.

//get result agent
+!start : true
    <-

   .wait(2000);
    .print("hello world, get result: ");
    getRequest("http://rorybucd.pythonanywhere.com/result", "", Code, Content);
    .print("Code: ", Code, " Content: ", Content);
    .


{ include("$jacamo/templates/common-cartago.asl") }