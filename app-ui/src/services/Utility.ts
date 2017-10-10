export class Utility {
    private static host : string = "https://uptoyouapp.com";//"http://localhost:8080";//
    static categories: string = "categories";
    static user : string = "user";

    //35.192.142.172

    public static getHost() : string {
        return this.host;
    }

    public static uuid() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
    }

    public static getCategories() {
        if(!localStorage.getItem(Utility.categories)) {
            console.log("No categories provided");
            return;
        }
        var cats = JSON.parse(localStorage.getItem(Utility.categories));
        var c = [];
        for(var i in cats)
            c.push(cats[i]['name']);
        return c;
    }

}