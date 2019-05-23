export class SecurityKey {
    constructor(
        public createDate?: number,
        public createLocation?: string,
        public fidoProtocol?: string,
        public fidoVersion?: string,
        public lastusedLocation?: string,
        public modifyDate?: number,
        public randomid?: string,
        public randomid_ttl_seconds?: string,
        public status?: string,
        public checked?: boolean) { }
}
