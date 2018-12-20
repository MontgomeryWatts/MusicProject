function checkNotEmpty(id){
    if(id){
        const element = document.getElementById(id);
        if(element) {
            const input = (element.value != null) ? element.value : "";
            if (!input) {
                alert("Please enter something into the " + element.name + " field.");
                return false;
            }
            return true;
        }
    }
    return false;
}