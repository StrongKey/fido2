export class SecurityKey {
    constructor(
        public credentialId?: string,
        public attestationFormat?: string,
        public createDate?: number,
        public createLocation?: string,
        public fidoProtocol?: string,
        public fidoVersion?: string,
        public displayName?: string,
        public lastusedLocation?: string,
        public modifyDate?: number,
        public keyid?: string,
        public randomid_ttl_seconds?: string,
        public status?: string,
        public checked?: boolean) { }
}
