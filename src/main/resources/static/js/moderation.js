$(".accept-btn").on("click", (e)=>{
    let ticketId = $(e.target).attr("data-ticketId");
    $.ajax({
        url: '/moderation/processTicket',
        type: 'POST',
        success: (x, y, _) => {
            console.log("success");
        },
        error: (x, y, _) => {
            //console.log("error");
        },
        complete: (x, y) => {
            //console.log("complete");
        },
        dataType : "json",
        data : {"ticketId" : ticketId, "action" : "ACCEPT"}
    });
    $("#row-" + ticketId).remove();
    console.log(ticketId)
});
$(".reject-btn").on("click", (e)=>{
    let ticketId = $(e.target).attr("data-ticketId");
    $.ajax({
        url: '/moderation/processTicket',
        type: 'POST',
        success: (x, y, _) => {
            console.log("success");
        },
        error: (x, y, _) => {
            //console.log("error");
        },
        complete: (x, y) => {
            //console.log("complete");
        },
        dataType : "json",
        data : {"ticketId" : ticketId, "action" : "REJECT"}
    });
    $("#row-" + ticketId).remove();
    console.log(ticketId)
});