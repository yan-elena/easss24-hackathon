!monitor.

+!monitor
    <-

   .wait(6000);
    .print("monitor agent, get argument strength of topic 1: ");

    !showArgumentStrength(0);
    !showArgumentStrength(1);

    .wait(6000);
    !monitor;
    .

+!showArgumentStrength(T)
    <-
    .concat("http://rorybucd.pythonanywhere.com/argument_strength/", T, URI);
    getRequest(URI, "", Code, Content);
    .println("argument strength for topic ",T, ":");
    .println(Content).



{ include("$jacamo/templates/common-cartago.asl") }