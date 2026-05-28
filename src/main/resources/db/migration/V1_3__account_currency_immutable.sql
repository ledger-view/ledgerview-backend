CREATE OR REPLACE FUNCTION ledgerview.prevent_account_currency_update()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.currency <> OLD.currency THEN
        RAISE EXCEPTION 'account currency cannot be changed after creation';
    END IF;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER accounts_currency_immutable
    BEFORE UPDATE ON ledgerview.accounts
    FOR EACH ROW EXECUTE FUNCTION ledgerview.prevent_account_currency_update();
