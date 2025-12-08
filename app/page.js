import Image from "next/image";
import { Navbar1 } from "@/components/navbar";
import { Hero7 } from "@/components/hero7";
export default function Home() {
  return (
    <>
          <Navbar1/>
    <div className="flex min-h-screen items-center justify-center bg-zinc-50 font-sans dark:bg-black">

      <main className="flex min-h-screen w-full max-w-3xl flex-col items-center justify-between py-32 px-16 bg-white dark:bg-black sm:items-start">
        <Hero7/>
      </main>
    </div>
    </>
  );
}
