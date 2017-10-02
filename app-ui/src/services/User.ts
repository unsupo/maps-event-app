export class User {
    userId : string;
    imageUrl : string;
    userGroup : string;
    locale : string;
    dateCreated : Date;
    dateLastLogin : Date;
    username: string;
    password: string;
    firstName : string;
    lastName : string;
    token : string;
    email: string;

    googleUser : any;

    eventsGoing: Array<number> = new Array();
    eventsMade: Array<number> = new Array();

    messageIds : Array<number> = new Array();
}